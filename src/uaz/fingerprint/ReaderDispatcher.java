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

    public void sendMessage(Message msg){
        synchronized(mQueue){
            if (!mQueue.contains(msg))
                mQueue.add(msg);
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
    }
    @Override
    public void run() {
        while(true){
            Message msg = pickMessage();
            if (msg != null){
                switch (msg.mCode) {
                    case Message.START_CAPTURE:
                        mIsCapturing = true;
                        mReader.nativeStartCapture(mCallback, msg);
                        break;
                    case Message.STOP_CAPTURE:
                        if (mIsCapturing){
                            mReader.nativeStopCapture(mCallback, msg);
                            mIsCapturing = false;
                        }
                        break;
                    case Message.OPEN:
                        mReader.nativeOpen();
                        break;
                    case Message.CLOSE:
                        mIsCapturing = false;
                        mReader.nativeClose();
                        break;
                    default:
                        break;
                }
            }
            if (mIsCapturing){
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
                return mCode == other.mCode;
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