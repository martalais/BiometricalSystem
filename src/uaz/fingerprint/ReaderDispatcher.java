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

/**
 *
 * @author Juan Hebert Chable Covarrubias
 * 
 */
public class ReaderDispatcher implements Runnable{
    final PriorityQueue<Message>  mQueue = new PriorityQueue<>();
    final Reader mReader;
    final NativeReaderCallback mCallback;
    private boolean mIsCapturing;
    private boolean mIsEnrolling;
    private boolean mIsOpen;
    public boolean sendMessage(Message msg){
        synchronized(mQueue){
            if (!mQueue.contains(msg))
                return mQueue.add(msg);
            return false;
        }
        
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
                System.out.println("Procesando Mensaje:" +msg.mCode + " \t" + Thread.currentThread().getName());
                switch (msg.mCode) {
                    case Message.START_CAPTURE:
                        if (!mIsCapturing && !mIsEnrolling && mIsOpen){
                            if (mReader.nativeStartCapture(mCallback, msg) < 0){
                                mCallback.onError(msg);
                            }
                            else{
                                mIsCapturing = true;
                                mCallback.onCaptureStart(msg);
                            }
                        }
                        break;
                    case Message.STOP_CAPTURE:
                        if (mIsCapturing){
                            if (mReader.nativeStopCapture(mCallback, msg) < 0){
                                mCallback.onError(msg);
                            }
                            mIsCapturing = false;
                        }
                        break;
                    case Message.START_ENROLLMENT:
                        if (!mIsCapturing && !mIsEnrolling && mIsOpen){
                            if (mReader.nativeStartEnrollment(mCallback, msg) < 0){
                                mCallback.onError(msg);
                            }
                            else{
                                mCallback.onCaptureStart(msg);
                                mIsEnrolling = true;
                            }                            
                        }
                        break;
                    case Message.STOP_ENROLLMENT:
                        if (mIsEnrolling){
                            if (mReader.nativeStopEnrollment(mCallback, mReader) < 0){
                                mCallback.onError(msg);
                            }
                        }                        
                        mIsEnrolling = false;
                        break;
                    case Message.OPEN:
                        if (!mIsOpen){
                            if (mReader.nativeOpen()  < 0){
                                mCallback.onError(msg);
                            }
                            else{
                                mIsOpen = true;
                                mCallback.onOpen(msg);
                            }                            
                        }                        
                        break;
                    case Message.CLOSE:
                        if (mIsEnrolling){
                            this.sendMessage(new Message(Message.STOP_ENROLLMENT, 0));
                            this.sendMessage(msg);
                        }
                        else if (mIsCapturing){
                            this.sendMessage(new Message(Message.STOP_CAPTURE, 0));
                            this.sendMessage(msg);
                        }
                        else if (mIsOpen){ 
                            if (mReader.nativeClose() < 0){
                                mCallback.onError(msg);
                            }else{
                                mIsOpen = false;
                                mCallback.onClose(msg);       
                            }
                        }
                        break;
                    case Message.CLOSE_DISPATCHER:
                        if (mIsOpen){
                            mReader.nativeClose();
                        }
                        break loop;
                    case Message.GET_ENROLL_STAGES:
                        if (mIsOpen){
                            mCallback.onGetEnrollStages(mReader.nativeGetNumberEnrollStages(), msg);
                        }
                        else{
                            //Necesitamos abrir el dispositvo primero para poder obtener informacion de el
                            this.sendMessage(new Message(Message.OPEN, 0));
                            this.sendMessage(new Message(Message.GET_ENROLL_STAGES, 1));
                        }
                        break;   
                    default:
                        break;
                }
            }
            if (mIsOpen && (mIsCapturing || mIsEnrolling)){
                mReader.nativeHandleEvents();
            }
        }
        
       
    }

}

class Message implements Comparable<Message>{
        public int mCode;
        public int mPriority;
        public static final int OPEN = 1;
        public static final int CLOSE = 2;
        public static final int START_CAPTURE = 3;
        public static final int STOP_CAPTURE = 4;
        public static final int START_ENROLLMENT = 5;
        public static final int STOP_ENROLLMENT = 6;
        public static final int GET_ENROLL_STAGES = 7;
        public static final int CLOSE_DISPATCHER = 8;
        public Message(int code, int priority){
            mCode = code;
            mPriority = priority;
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