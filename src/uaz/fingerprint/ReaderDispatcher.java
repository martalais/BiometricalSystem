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

import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan Hebert Chable Covarrubias
 * 
 */
public class ReaderDispatcher implements Runnable{
    final PriorityQueue<Message>  mQueue = new PriorityQueue<>();
    final TreeMap<Message, MessageResult> mWaitingResults = new TreeMap<>();
    final Reader mReader;
    final NativeReaderCallback mCallback;
    private boolean mIsCapturing;
    private boolean mIsEnrolling;
    private boolean mIsOpen;
    
    public MessageResult sendMessage(Message msg){
        boolean inserted = false;
        synchronized(mQueue){
            if (!mQueue.contains(msg)){
                inserted =  mQueue.add(msg);
                mQueue.notify();
            }
        }        
        if (inserted && msg.mWaitable){
            synchronized(mWaitingResults){
                MessageResult result = null;
                while((result = mWaitingResults.remove(msg)) == null){
                    try {
                        mWaitingResults.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ReaderDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return result;
            }
        }
        return null;
    }

    private Message pickMessage(){
        synchronized(mQueue){
            return mQueue.poll();
        }
    }

    
    public ReaderDispatcher(Reader reader, NativeReaderCallback callback){
        mReader = reader;
        mCallback = callback;
        mIsCapturing = false;
        mIsEnrolling = false;
        mIsOpen = false;
    }
    @Override
    public void run() {
        loop: while(true){
            Message msg = pickMessage();
            if (msg != null ){
                //System.out.println("Procesando Mensaje:" +msg.mCode + " \t" + Thread.currentThread().getName());
                Object ret = null;
                int code = MessageResult.IGNORED;
                switch (msg.mCode) {
                    case Message.START_CAPTURE:
                        if (!mIsCapturing && !mIsEnrolling && mIsOpen){
                            if (mReader.nativeStartCapture(mCallback, msg) < 0){
                                mCallback.onError(msg);
                                code = MessageResult.FAIL;
                            }
                            else{
                                mIsCapturing = true;
                                mCallback.onCaptureStart(msg);
                                code = MessageResult.SUCCESS;
                            }
                        }
                        break;
                    case Message.STOP_CAPTURE:
                        if (mIsCapturing){
                            if (mReader.nativeStopCapture(mCallback, msg) < 0){
                                mCallback.onError(msg);
                                code = MessageResult.FAIL;
                            }
                            else{
                                code = MessageResult.SUCCESS;
                            }
                            mIsCapturing = false;
                        }
                        break;
                    case Message.START_ENROLLMENT:
                        if (!mIsCapturing && !mIsEnrolling && mIsOpen){
                            if (mReader.nativeStartEnrollment(mCallback, msg) < 0){
                                mCallback.onError(msg);
                                code = MessageResult.FAIL;
                            }
                            else{
                                mCallback.onCaptureStart(msg);
                                mIsEnrolling = true;
                                code = MessageResult.SUCCESS;
                            }                            
                        }
                        break;
                    case Message.STOP_ENROLLMENT:
                        if (mIsEnrolling){
                            if (mReader.nativeStopEnrollment(mCallback, mReader) < 0){
                                mCallback.onError(msg);
                                code = MessageResult.FAIL;
                            }
                            else{
                                code = MessageResult.SUCCESS;
                            }
                        }                        
                        mIsEnrolling = false;
                        break;
                    case Message.OPEN:
                        if (!mIsOpen){
                            if (mReader.nativeOpen()  < 0){
                                mCallback.onError(msg);
                                code = MessageResult.FAIL;
                            }
                            else{
                                mIsOpen = true;
                                mCallback.onOpen(msg);
                                code = MessageResult.SUCCESS;
                            }                            
                        }                        
                        break;
                    case Message.CLOSE:
                        if (mIsOpen){ 
                            if (mReader.nativeClose() < 0){
                                mCallback.onError(msg);
                                code = MessageResult.FAIL;
                            }else{
                                mIsOpen = false;
                                mCallback.onClose(msg);
                                code = MessageResult.SUCCESS;
                            }
                        }
                        break;
                    case Message.CLOSE_DISPATCHER:
                        if (mIsOpen){
                            mReader.nativeClose();
                            code = MessageResult.SUCCESS;
                        }
                        break loop;
                    case Message.GET_ENROLL_STAGES:
                        if (mIsOpen){
                            int result = mReader.nativeGetNumberEnrollStages();
                            mCallback.onGetEnrollStages(result, msg);
                            code = MessageResult.SUCCESS;
                            ret = new Integer(result);
                        }
                        else{
                            //Necesitamos abrir el dispositvo primero para poder obtener informacion de el
                            mCallback.onError(msg);
                            code = MessageResult.FAIL;
                        }
                        break;   
                    case Message.GET_DRIVER_NAME:
                        if (mIsOpen){
                            String name = mReader.nativeGetDriverName();
                            mCallback.onGetDriverName(name, msg);                            
                            ret = name;
                            code = MessageResult.SUCCESS;
                        }
                        else{
                            mCallback.onError(msg);
                            code = MessageResult.FAIL;
                        }
                        break;
                    default:
                        break;
                }
                
                if (msg.mWaitable){
                    MessageResult result = new MessageResult();
                    result.code = code;
                    result.result = ret;
                    synchronized(mWaitingResults){
                        mWaitingResults.put(msg, result);
                        mWaitingResults.notify();
                    }
                }
            }
            //Si estamos en proceso de captura, revisamos que la huella este lista
            if (mIsOpen){
                mReader.nativeHandleEvents(0);
            }            
        }
        
       
    }

}
class MessageResult{
    public final static int SUCCESS = 0;
    public final static int FAIL = 1;
    public final static int IGNORED = 2;
    int code;
    Object result;
}
class Message implements Comparable<Message>{
        public int mCode;
        public int mPriority;
        public boolean mWaitable;
        public static final int OPEN = 1;
        public static final int CLOSE = 2;
        public static final int START_CAPTURE = 3;
        public static final int STOP_CAPTURE = 4;
        public static final int START_ENROLLMENT = 5;
        public static final int STOP_ENROLLMENT = 6;
        public static final int GET_ENROLL_STAGES = 7;
        public static final int GET_DRIVER_NAME = 8;
        public static final int CLOSE_DISPATCHER = 9;
        public static final int MESSAGE_LOW_PRIORITY = 10;
        public static final int MESSAGE_NORMAL_PRIORITY = 5;
        public static final int MESSAGE_HIGH_PRIORITY = 0;
        public Message(int code, int priority, boolean waitable){
            mCode = code;
            mPriority = priority;
            mWaitable = waitable;
        }
        public Message(int code, int priority){
            this(code, priority, false);
        }
        @Override
        public int compareTo(Message other) {
            if (other == null){
                return -1;
            }
            return mPriority - other.mPriority;
        }
        @Override 
        public boolean equals(Object obj ){
            if (obj != null && obj instanceof Message){
                Message other = (Message) obj;
                return mCode == other.mCode && mPriority == other.mPriority;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.mCode;
            hash = 97 * hash + this.mPriority;
            return hash;
        }
        
    }