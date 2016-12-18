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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import uaz.nativeutils.NativeUtils;

/**
 * 
 * @author xmbeat
 */
public class Reader implements NativeReaderCallback{
    private final NativeReader mNativeReader;
    private final NativeReaderManager mManager;
    private final ArrayList<ReaderListener> mListeners;
    private String mDriverName;
    private Integer mEnrollStages;
  
    static {
        try {
            System.loadLibrary("fingerprint"); // used for tests. This library in classpath only
            NativeReader.init();
        } catch (UnsatisfiedLinkError e) {
            try {
                NativeUtils.loadLibraryFromJar("/nativelibs/" + System.mapLibraryName("fingerprint"));
                NativeReader.init();
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }
    }
    
    
    /**
     * Crea he inicializa este lector.
     * @param reader dispositivo que se usara para realizar las operaciones
     */
    private Reader(NativeReader reader){
        mNativeReader = reader;
        mListeners = new ArrayList<>();
        mManager = new NativeReaderManager(reader);
        mManager.addListener(this);
    }
    
    /**
     * Obtiene el nombre del driver del dispositivo lector
     * @return el nombre del driver del dispositivo si ha sido abierto con anterioridad
     * @throws ReaderException si el dispositivo no se ha abierto.
     */
    public String getDriverName() throws ReaderException{
        if (mDriverName == null){
            throw ReaderException.DEVICE_NOT_OPENED;
        }
        return mDriverName;
    }
    
    @Override
    public String toString(){
        return mDriverName;
    }
    
    public int getEnrollStages(){
        if (mEnrollStages == null)
            throw ReaderException.DEVICE_NOT_OPENED;
        return mEnrollStages.intValue();
    }

    
    public void addListener(ReaderListener listener){
        mListeners.add(listener);
    }
    
    
    public static Reader getDefault(){
        List<NativeReader> readers = NativeReader.listDevices();
        if (readers != null && readers.size() > 0){
            Reader reader = new Reader(readers.get(0));
            
            return reader;
        }
        return null;
    }
    
    /**
     * Llama al método nativo que obtiene una lista de dispositivos lectores 
     * de huellas dactilares 
     * @return una lista con los dispositivos lectores disponibles o null si no 
     * hay disponibles.
     */
    public static List<Reader> listDevices(){
        List<NativeReader> readers = NativeReader.listDevices();
        List<Reader> result = new ArrayList<>();
        if (readers != null && readers.size() > 0){
            for (int i = 0; i < readers.size(); i++){
                Reader reader = new Reader(readers.get(i));
                result.add(reader);
            }
            return result;
        }
        return result;
    }
    
    public void setDaemon(boolean value){
        mManager.setDaemon(value);
    }
    
    /**
     * Obtiene acceso al dispositivo para poder ejectuar acciones sobre éste.
     * @throws ReaderException Si el dispositivo ya esta siendo ocupado en otro
     * contexto o proceso.
     */
    public void open() throws ReaderException{
        Message msg = new Message(Message.OPEN, Message.MESSAGE_NORMAL_PRIORITY, true);
        MessageResult result = mManager.sendMessage(msg);
        if (result != null && result.code == MessageResult.FAIL){
            throw ReaderException.DEVICE_BUSSY;
        }
        if (mDriverName == null){
            msg = new Message(Message.GET_DRIVER_NAME, Message.MESSAGE_NORMAL_PRIORITY, true);
            result = mManager.sendMessage(msg);
            if (result.code == MessageResult.FAIL){
                throw ReaderException.DEVICE_NOT_OPENED;
            }
            else{
                mDriverName = (String)result.result;
            }
        }
        if (mEnrollStages == null){
            msg = new Message(Message.GET_ENROLL_STAGES, Message.MESSAGE_NORMAL_PRIORITY, true);
            result = mManager.sendMessage(msg);
            if (result.code == MessageResult.FAIL)
                throw ReaderException.DEVICE_NOT_OPENED;
            else
                mEnrollStages = (Integer) result.result;
        }
    }
    /**
     * Cierra el dispositivo si fue creado, liberando los recursos suficientes 
     * para que pueda ser usado en otro contexto.
     * @throws ReaderException
     */
    public void close () throws ReaderException{
        Message msg = new Message(Message.CLOSE, Message.MESSAGE_NORMAL_PRIORITY, true);
        MessageResult result = mManager.sendMessage(msg);
        if (result != null && result.code == MessageResult.FAIL){
            throw ReaderException.DEVICE_NOT_OPENED;
        }
    }
    /**
     * 
     * @throws ReaderException 
     */
    public void startCapture() throws ReaderException{
        Message msg = new Message(Message.START_CAPTURE, Message.MESSAGE_NORMAL_PRIORITY, true);
        MessageResult result = mManager.sendMessage(msg);
        if (result != null && result.code == MessageResult.FAIL){
            throw ReaderException.DEVICE_NOT_OPENED;
        }
    }
    public void startEnrollment() throws ReaderException{
        Message msg = new Message(Message.START_ENROLL, Message.MESSAGE_NORMAL_PRIORITY, true);
        MessageResult result = mManager.sendMessage(msg);
        if (result != null && result.code == MessageResult.FAIL){
            throw ReaderException.DEVICE_NOT_OPENED;
        }
    }
    
    /**
     * 
     * @throws ReaderException 
     */
    public void stopCapture() throws ReaderException{
        Message msg = new Message(Message.STOP_CAPTURE, Message.MESSAGE_NORMAL_PRIORITY, true);
        MessageResult result = mManager.sendMessage(msg);
        if (result != null && result.code == MessageResult.FAIL){
            throw ReaderException.DEVICE_NOT_OPENED;
        }
    }
    public void stopEnrollment() throws ReaderException{
        Message msg = new Message(Message.STOP_ENROLL, Message.MESSAGE_NORMAL_PRIORITY, true);
        MessageResult result = mManager.sendMessage(msg);
        if (result != null && result.code == MessageResult.FAIL){
            throw ReaderException.DEVICE_NOT_OPENED;
        } 
    }
    
    public static VerifyResult verify(byte[] printData, byte[] anotherPrintData){
        //No importa desde que hilo se ejecute, esta funcion no depende de ningun dispositivo
        return NativeReader.verify(printData, anotherPrintData);
    }
    public void startVerify(byte[] printData){
        Message msg = new Message(Message.START_VERIFY, Message.MESSAGE_NORMAL_PRIORITY, true);
        msg.mAdditionalData = printData;
        MessageResult result = mManager.sendMessage(msg);
        if (result != null && result.code == MessageResult.FAIL)
            throw ReaderException.DEVICE_NOT_OPENED;
    }
    public void stopVerify(){
        Message msg = new Message(Message.STOP_VERIFY, Message.MESSAGE_NORMAL_PRIORITY, true);
        MessageResult result = mManager.sendMessage(msg);
        if (result != null && result.code == MessageResult.FAIL)
            throw ReaderException.DEVICE_NOT_OPENED;
    }
    /**
     * Ejecuta un nuevo escaneo en el dispositivo y lo compara con printData
     * @param printData la huella que se comparará con la resultante del escaneo
     * @return un objeto VerifyResult con los datos de la comparación
     */
    public VerifyResult verify(byte[] printData){
        Message msg = new Message(Message.VERIFY, Message.MESSAGE_NORMAL_PRIORITY, true);
        msg.mAdditionalData = printData;
        MessageResult result = mManager.sendMessage(msg);
        if (result != null){
            if (result.code == MessageResult.SUCCESS){
                return (VerifyResult)result.result;
            }
            else{
                throw ReaderException.DEVICE_NOT_OPENED;
            }
        }
        return null;
    }
    public VerifyResult identify(){
        return null;
    }
    
    @Override
    public void onEnroll(EnrollResult enroll, Object userData) {
        System.out.println("On Enroll");
        for (ReaderListener listener: mListeners){
            listener.onEnroll(this, enroll);
        }
    }

    @Override
    public void onEnrollStart(Object userData) {
        System.out.println("On Enroll Start");
        for (ReaderListener listener: mListeners){
            listener.onEnrollStart(this);
        }
    }

    @Override
    public void onEnrollStop(Object userData) {
        System.out.println("On Enroll stop");
        for (ReaderListener listener: mListeners){
            listener.onEnrollStop(this);
        }
    }

    @Override
    public void onCapture(EnrollResult result, Object userdata) {
        System.out.println("FingerprintReader::onCapture");
        for (ReaderListener listener: mListeners){
            listener.onCapture(this, result);
        }
    }

    @Override
    public void onCaptureStart(Object userData) {
        System.out.println("FingerprintReader::onCaptureStart");
        for (ReaderListener listener: mListeners){
            listener.onCaptureStart(this);
        }
    }

    @Override
    public void onCaptureStop(Object userData) {
        System.out.println("FingerprintReader::onCaptureStop");
        for (ReaderListener listener: mListeners){
            listener.onCaptureStop(this);
        }
    }
    
    
        
    
}
