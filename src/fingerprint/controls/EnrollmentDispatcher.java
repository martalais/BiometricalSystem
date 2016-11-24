/*
 * The MIT License
 *
 * Copyright 2016 Juan Hebert Chablé Covarrubias.
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
package fingerprint.controls;

import java.util.ArrayList;
import uaz.fingerprint.EnrollResult;
import uaz.fingerprint.Reader;

/**
 *
 * @author Juan Hebert Chable Covarrubias
 * Clase encargada de convertir un objeto uaz.fingerprint.Reader en uno que
 * envie Huellas digitales de manera asincrona a los Listener. 
 * Los eventos disparados desde aqui se hacen desde un hilo nuevo, por lo que 
 * si se utilizan para actualizar algo de la GUI necesitan usar metodos como 
 * invokeLater de Swing o Platform.runLater de las librerias javafx.
 */
public class EnrollmentDispatcher {
    private Reader mReader;
    private Dispatcher mDispatcher;
    private ArrayList<EnrollmentListener> mListeners;
    public EnrollmentDispatcher(Reader reader){
        mReader = reader;
        mDispatcher = new Dispatcher(reader);
    }
    
    public void start(){
        if (!mDispatcher.isRunning()){
            new Thread(mDispatcher).start();
        }
    }
    
    public void stop(){
        if (mDispatcher.isRunning()){
            mDispatcher.stop();
        }
    }
    
    public void addListener(EnrollmentListener listener){
        synchronized(mListeners){
            mListeners.add(listener);
        }
    }
    
    private class Dispatcher implements Runnable{
        private Reader mReader;
        private boolean mStop;
        private boolean mIsRunning;
        public Dispatcher(Reader reader){
            mReader = reader;
            mListeners = new ArrayList<EnrollmentListener>();
            mStop = false;
        }

        public boolean isRunning(){
            return mIsRunning;
        }
        public void stop(){
           mStop = true;
           mReader.close();
        }
        public void run (){
            mStop = false;
            mIsRunning = true;
            mReader.open();
            synchronized(mListeners){
                for (EnrollmentListener listener: mListeners){
                    listener.onStart(mReader);
                }
            }
            while(!mStop){
                EnrollResult result = mReader.enrollFinger();
                synchronized(mListeners){
                    for (EnrollmentListener listener: mListeners){
                        listener.onCapture(mReader, result);
                    }
                }
            }
            synchronized(mListeners){
                for (EnrollmentListener listener: mListeners){
                    listener.onClose(mReader);
                }
            }
            mIsRunning = false;
        }
    }
    public static interface EnrollmentListener{
        public void onStart(Reader reader);
        public void onCapture(Reader reader, EnrollResult result);
        public void onClose(Reader reader);
    }
}
