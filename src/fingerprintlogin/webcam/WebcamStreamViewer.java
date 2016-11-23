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
package fingerprintlogin.webcam;

import com.github.sarxos.webcam.Webcam;
import fingerprintlogin.CanvasImageViewer;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import javafx.application.Platform;
import javafx.concurrent.Task;


public class WebcamStreamViewer extends Task<Void> {

    private boolean mPlay;
    private boolean mStop;
    private BufferedImage mLastImage;
    private Webcam mWebcam;
    private CanvasImageViewer mCanvas;
    private WebcamStreamListener mListener;
    
    public WebcamStreamViewer(Webcam webcam, CanvasImageViewer canvas, WebcamStreamListener listener){
        mPlay = true;
        mStop = false;
        mWebcam = webcam;
        mCanvas = canvas; 
        mListener = listener;
    }
    
    public WebcamStreamViewer(Webcam webcam, CanvasImageViewer canvas) {
       this(webcam, canvas, null);
    }
    
    public void start(){
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public BufferedImage getLastImage() {
        return mLastImage;
    }

    public boolean isPaused() {
        return !mPlay;
    }

    public void play() {
        mPlay = true;
    }

    public void pause() {
        mPlay = false;
    }

    public void stop() {
        mStop = true;
    }

    @Override
    protected Void call() throws Exception {
        Runnable imageUpdater = () -> {
            mCanvas.updateDraw();
        };
        mWebcam.open();
        if (mListener != null){
            Platform.runLater(() -> {
                mListener.onStart(mWebcam); 
            });
        }
        Dimension dimension = mWebcam.getViewSize();
        ByteBuffer buffer = ByteBuffer.allocateDirect(dimension.width * dimension.height * 4);
        while (!mStop) {

            if (mPlay) {
                //mLastImage = mWebcam.getImage();
                mWebcam.getImageBytes(buffer);
                System.out.println("Image received");
                //mCanvas.setImage(mLastImage, false);
                //Platform.runLater(imageUpdater);
            } else {
                Thread.sleep(100);
            }
        }
        mWebcam.close();
        if (mListener != null){
            Platform.runLater(() -> {
                mListener.onStop(mWebcam); 
            });
        }
        return null;
    }
    public static interface WebcamStreamListener{
        public void onStart(Webcam webcam);
        public void onStop(Webcam webcam);
    }
}
