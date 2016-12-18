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

import java.util.List;

/**
 *
 * @author xmbeat
 */
public class NativeReader {
    private long mReaderDataPointer;
    
    public static native List<NativeReader> listDevices();
    public static native int init();
    public static native void exit();
    public native int open();
    public native int close();
    public native String getDriverName();
    public native int getEnrollStages();
    public native int startCapture(NativeReaderCallback callback, Object userData);
    public native int stopCapture(NativeReaderCallback callback, Object userData);
    public native int startEnrollment(NativeReaderCallback callback, Object userData);
    public native int stopEnrollment(NativeReaderCallback callback, Object userData);
    public native int handleEvents(int waitTime);
    public native VerifyResult verify(byte[] data);
    public native static VerifyResult verify(byte[] print, byte[] anotherprint);
    
    public native int freeResources();
    
    @Override
    protected void finalize() throws Throwable {
        //Liberamos los recursos que se usaron en codigo nativo
        this.freeResources();            
        super.finalize();
    }
}
