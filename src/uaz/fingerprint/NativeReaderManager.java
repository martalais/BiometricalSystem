/*
 * The MIT License
 *
 * Copyright 2016 xmbeat.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package uaz.fingerprint;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan Hebert Chable Covarrubias Clase encargada de realizar
 * operaciones sobre un objeto NativeReader de manera que todas las operaciones
 * sean thread safe, debido a que la libreria "libfprint" no implementa este
 * mecanismo y si es utilizado desde varios hilos puede terminar en la
 * corrupcion de la maquina virtual JVM.
 * Este bind de la librería además soporta las llamadas a las funciones del reader
 * dentro del mismo hilo en el que se llaman a los callbacks sin que exista un 
 * dead lock.
 * Consideraciones de la implementación:
 *  -FIXED: llamar las funciones nativas dentro de una llamada a handleEvents
 *  -FIXED: llamar las funciones nativas dentro de los callbacks
 *  -FIXED: Esperar por los resultados (MessageResult) de un mensaje(Message) 
 * TODO:
 *  -agregar funcionalidad para verify, identify y open asincronos 
 *  -sincronizar la creacion del worker por si hay 2 llamadas a open simultaneas
 *  -Creación de un hilo universal para las llamadas multiples NativeReader
 */
public class NativeReaderManager implements Runnable, NativeReaderCallback {

    private PriorityQueue<Message> mQueue = new PriorityQueue<>();
    private TreeMap<Message, MessageResult> mResults = new TreeMap<>();
    private ArrayList<NativeReaderCallback> mListeners = new ArrayList<>();
    private static final int ENROLLING = 1;
    private static final int CAPTURING = 2;
    private static final int STOPING = 3;
    private static final int OPENING = 4;
    private EnrollResult mLastCapture;
    private Message mLastMessage = new Message(Message.NONE, Message.MESSAGE_NORMAL_PRIORITY);
    private Thread mWorker;
    private boolean mIsDaemon = false;
    private boolean mCanceled = false;
    private boolean mIsOpened = false;
    private int mCurrentTask = 0;
    private NativeReader mReader;

    public NativeReaderManager(NativeReader reader) {
        mReader = reader;
    }

    public void setDaemon(boolean value) {
        mIsDaemon = value;
    }

    public void addListener(NativeReaderCallback listener) {
        mListeners.add(listener);
    }

    private boolean isOpened() {
        return mIsOpened;
    }

    private Message pickMessage() {
        synchronized (mQueue) {
            return mQueue.poll();
        }
    }

    /**
     * Envía un mensaje a la cola de mensajes para ser procesado, este método
     * bloquea el hilo actual si el mensaje lo requiere.
     *
     * @param msg el mensaje que encolar
     * @return Un objeto de tipo MessageResult cuyo contenido varia dependiendo
     * el tipo de mensaje encolado o null si el mensaje no es esperable
     * (mWaitable)
     */
    public MessageResult sendMessage(Message msg) {
        boolean inserted = false;
        //El dispositivo no está abierto, por lo tanto el main loop thread tampoco
        if (!mIsOpened) {
            if (msg.mCode == Message.OPEN) {
                mWorker = new Thread(this);
                mWorker.setDaemon(mIsDaemon);
                mWorker.start();
            } else {
                throw ReaderException.DEVICE_NOT_OPENED;
            }
        }

        synchronized (mQueue) {
            if (mQueue.add(msg)) {
                //Notifica al consumidor (blucle de mensaje) que hay un nuevo mensaje
                mQueue.notify();
                inserted = true;
            }
        }

        //Se pudo encolar el mensaje y ademas se tiene que esperar por el resultado
        if (inserted && msg.mWaitable) {
            MessageResult result;
            //Si se trata de esperar en el mismo hilo que el productor
            //Iteramos el bucle principal del productor hasta que procese el mensaje
            //pero sin usar los metodos de sincronizacion puesto que ocasionaria un dead lock
            if (Thread.currentThread().getId() == mWorker.getId()) {
                while ((result = mResults.remove(msg)) == null && !mCanceled) {
                    digestMessage();
                }

            } else {
                synchronized (mResults) {
                    //Mientras no hay respuesta se duerme el hilo actual en espera de respuesta
                    while ((result = mResults.remove(msg)) == null) {
                        try {
                            mResults.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(NativeReaderManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            return result;
        }
        return null;
    }

    private MessageResult digestMessage() {
        Message msg = pickMessage();
        if (msg != null) {
            MessageResult result = new MessageResult();
            result.code = MessageResult.IGNORED;
            System.out.println("Processing msg: " + msg);
            switch (msg.mCode) {
                case Message.OPEN:
                    if (!mIsOpened) {
                        if (mReader.open() == 0) {
                            result.code = MessageResult.SUCCESS;
                            mIsOpened = true;
                        } else {
                            mCanceled = true;
                            result.code = MessageResult.FAIL;
                        }
                    }
                    break;
                case Message.CLOSE:
                    if (mIsOpened) {
                        if (mReader.close() == 0) {
                            result.code = MessageResult.SUCCESS;
                            mIsOpened = false;
                            mCanceled = true;
                            mCurrentTask = 0;
                        } else {
                            result.code = MessageResult.FAIL;
                        }
                    }
                    break;
                case Message.START_CAPTURE:
                    if (mIsOpened && mCurrentTask == 0) {
                        if (mReader.startCapture(this, msg) == 0) {
                            result.code = MessageResult.SUCCESS;
                            mCurrentTask = CAPTURING;
                            this.onCaptureStart(msg);
                        } else {
                            result.code = MessageResult.FAIL;
                        }
                    }
                    break;
                case Message.STOP_CAPTURE:
                    if (mIsOpened && mCurrentTask == CAPTURING) {
                        if (mReader.stopCapture(this, result) == 0) {
                            //Esperamos a que la funcion callback se ejecute y 
                            //Modifique el valor de mLastMessage
                            while (mLastMessage.mCode != Message.STOP_CAPTURE) {
                                if (mReader.handleEvents(0) != 0) {
                                    mLastMessage.mCode = Message.NONE;
                                    result.code = MessageResult.FAIL;
                                    break;
                                }
                            }
                            mLastMessage.mCode = Message.NONE;
                            result.code = MessageResult.SUCCESS;
                            for (NativeReaderCallback listener : mListeners){
                                listener.onCaptureStop(null);
                            }
                        } else {
                            result.code = MessageResult.FAIL;
                        }
                        mCurrentTask = 0;
                    }
                    break;
                case Message.START_ENROLL:
                    if (mIsOpened && mCurrentTask == 0) {
                        if (mReader.startEnrollment(this, msg) == 0) {
                            result.code = MessageResult.SUCCESS;
                            mCurrentTask = ENROLLING;
                            this.onEnrollStart(msg);
                        } else {
                            result.code = MessageResult.FAIL;
                        }
                    }
                    break;
                case Message.STOP_ENROLL:
                    if (mIsOpened && mCurrentTask == ENROLLING) {
                        if (mReader.stopEnrollment(this, result) == 0) {
                            //Esperamos a que la funcion callback se ejecute
                            while (mLastMessage.mCode != Message.STOP_ENROLL) {
                                if (mReader.handleEvents(0) < 0) {
                                    mLastMessage.mCode = Message.NONE;
                                    result.code = MessageResult.FAIL;
                                    break;
                                }
                            }
                            mLastMessage.mCode = Message.NONE;
                            result.code = MessageResult.SUCCESS;
                            for (NativeReaderCallback listener : mListeners){
                                listener.onEnrollStop(null);
                            }
                        } else {
                            result.code = MessageResult.FAIL;
                        }
                        mCurrentTask = 0;
                    }
                    break;
                case Message.GET_DRIVER_NAME:
                    if (mIsOpened) {
                        String name = mReader.getDriverName();
                        result.code = MessageResult.SUCCESS;
                        result.result = name;
                    } else {
                        result.code = MessageResult.FAIL;
                    }
                    break;
                case Message.GET_ENROLL_STAGES:
                    if (mIsOpened) {
                        Integer num = mReader.getEnrollStages();
                        result.code = MessageResult.SUCCESS;
                        result.result = num;
                    } else {
                        result.code = MessageResult.FAIL;
                    }
                    break;
            }

            if (msg.mWaitable) {
                synchronized (mResults) {
                    mResults.put(msg, result);
                    mResults.notify();
                }
            }

            System.out.println("Procesado! msg: " + msg);
            return result;
        } else if (mCurrentTask != 0) {
            if (mReader.handleEvents(0) == 0 && mLastMessage.mCode != Message.NONE) {
                for (NativeReaderCallback listener: mListeners){
                    switch(mLastMessage.mCode){
                        case Message.CAPTURE:
                            mLastMessage.mCode = Message.NONE;
                            listener.onCapture(mLastCapture, null);
                            break;
                        case Message.ENROLL:
                            mLastMessage.mCode = Message.NONE;
                            listener.onEnroll(mLastCapture, null);
                            //Se ejecutó el ultimo enroll
                            if (mLastCapture.getCode() == EnrollResult.COMPLETE){
                                //Mandamos la señal stop enroll
                                sendMessage(new Message(Message.STOP_ENROLL, Message.MESSAGE_NORMAL_PRIORITY, true));
                            }
                            break;
                        case Message.VERIFY:
                            mLastMessage.mCode = Message.NONE;
                            
                            break;
                    }
                }
               
            }

        }
        return null;
    }

    @Override
    public void run() {
        mCanceled = false;
        while (!mCanceled) {
            digestMessage();
        }
    }
    
    //Llamado desde NativeReader.handleEvents
    @Override
    public void onEnroll(EnrollResult enroll, Object userData) {
        mLastCapture = enroll;
        mLastMessage.mCode = Message.ENROLL;
    }
    
    //Llamado desde NativeReaderManager.digestMessage
    @Override
    public void onEnrollStart(Object userData) {
        for (NativeReaderCallback listener : mListeners) {
            listener.onEnrollStart(userData);
        }
    }
    
    //Llamado desde NativeReader.handleEvents
    @Override
    public void onEnrollStop(Object userData) {
       mLastMessage.mCode = Message.STOP_ENROLL;
    }
    
    //Llamado desde NativeReader.handleEvents
    @Override
    public void onCapture(EnrollResult result, Object userdata) {
        mLastCapture = result;
        mLastMessage.mCode = Message.CAPTURE;
    }
    
    //Llamado desde NativeReaderManager.digestMessage
    @Override
    public void onCaptureStart(Object userData) {
        for (NativeReaderCallback listener : mListeners) {
            listener.onCaptureStart(userData);
        }
    }
    
    //Llamado desde NativeReader.handleEvents   
    @Override
    public void onCaptureStop(Object userData) {
        mLastMessage.mCode = Message.STOP_CAPTURE;
    }

}
