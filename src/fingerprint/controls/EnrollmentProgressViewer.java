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

import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import uaz.fingerprint.EnrollResult;
import uaz.fingerprint.FingerprintImage;
import uaz.fingerprint.Reader;
import uaz.fingerprint.ReaderListener;

/**
 *
 * @author Juan Hebert Chablé Covarrubias
 */
public class EnrollmentProgressViewer extends Canvas  {
    private Color mBackground = new Color(0.2, 0.3, 0.3, 1);
    private Color mFailBall = new Color(0.8, 0.2, 0.2, 1.0);
    private Color mFilledBall = new Color(0.2, 0.8, 0.2, 1.0);
    private Color mEmptyBall = new Color(0.2, 0.5, 0.5, 1.0);
    private Effect mInnerShadow = new InnerShadow(5.0, Color.BLACK);
    private Effect mDropShadow = new DropShadow(5.0, Color.BLACK);
    private Effect mImageShadow = new DropShadow(5.0, mFilledBall);
    private Color mTextColor = new Color(1,1,1,1);
    private int mTotalProgress = 0;
    private int mProgress = 0;
    private int mStatus = 0;
    private Reader mReader;
    private WritableImage[] mThumbs;
    
    public void increment(int val){
        setProgress(mProgress + val);
    }
    
    public void setStatus(int status){
        mStatus = status;
        updateDraw();
    }
    public EnrollmentProgressViewer(Reader reader){
        mReader= reader;
        reader.addListener(new ReaderListener() {
            @Override
            public void onError(Reader reader, int code){
                Platform.runLater(() -> {                    
                    setStatus(1);
                    setProgress(0);
                });
            }
            @Override
            public void onStartCapture(Reader reader) {
                System.out.println("EPV:OnStartCapture()");
                Platform.runLater(() -> {                    
                    setStatus(0);
                    setTotalProgress(reader.getNumberEnrollStages());
                    setProgress(0);
                    mThumbs = new WritableImage[mTotalProgress];
                });
            }

            @Override
            public void onCapture(Reader reader, EnrollResult result) {
                
                if (result.getCode() != EnrollResult.FAIL){
                    Platform.runLater(()->{
                        if (result.getImage() != null ){
                            FingerprintImage img = result.getImage();
                            BufferedImage bimg = img.toBufferedImage();
                            WritableImage thumb = SwingFXUtils.toFXImage(bimg, null);
                            mThumbs[mProgress] = thumb;
                        }
                        else{
                            mThumbs[mProgress] = null;
                        }
                    
                    });
                    
                }
                if (result.getCode() == EnrollResult.COMPLETE ){
                    //Draw progress
                     Platform.runLater(() -> {
                        setStatus(0);
                        increment(1);
                    });
                }
                else if(result.getCode() == EnrollResult.PASS ){
                    //Draw progress
                    Platform.runLater(() -> {
                        setStatus(0);
                        increment(1);
                    });
                    
                }
                else if (result.getCode() == EnrollResult.FAIL){
                    //Draw all ball with red status
                     Platform.runLater(() -> {                         
                         setStatus(1);
                         setProgress(0);
                    });
                }
                else{
                    //Draw next ball with red status
                     Platform.runLater(() -> {
                         setStatus(2);
                    });
                }
                
                
            }

            @Override
            public void onStopCapture(Reader reader) {
                
            }

            @Override
            public void onClose(Reader reader) {
                Platform.runLater(() -> {                    
                    setStatus(0);
                    setProgress(0);
                });
            }

            @Override
            public void onOpen(Reader reader) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }
    
    public void setReader(Reader reader){
        mReader = reader;
    }  

    public Reader getReader(){
        return mReader;
    }
    
    public void setTotalProgress(int value){
        mTotalProgress = value;
        if (mProgress > mTotalProgress){
            mProgress = mTotalProgress;
        }
        updateDraw();
    }
    public void setProgress(int value){
        if (mTotalProgress < value){
            mProgress = mTotalProgress;
        }
        else if (value < 0){
            mProgress = 0;
        }
        else{
            mProgress = value;
        }
        updateDraw();
    }
    
    @Override
    public double minHeight(double width)
    {
        return 100;
    }

    @Override
    public double maxHeight(double width)
    {
        return Double.MAX_VALUE;
    }

    @Override
    public double prefHeight(double width)
    {
        return minHeight(width);
    }

    @Override
    public double minWidth(double height)
    {
        return 64;
    }

    @Override
    public double maxWidth(double height)
    {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean isResizable(){
        return true;
    }
    
    @Override
    public void resize(double width, double height){
        super.setWidth(width);
        super.setHeight(height);
        updateDraw();
    }
    

   
    
    public void updateDraw(){
        GraphicsContext gc = getGraphicsContext2D();
        
        double width = getWidth();
        double height = getHeight();
        double spaceRatio = 0.3; //Relacion del espacio respecto a las bolitas
        double circleSize = width / ((mTotalProgress + 1) * spaceRatio + mTotalProgress);
        double boxWidth = width;
        double boxHeight = circleSize * 2 + 3 * circleSize * spaceRatio;
        double boxRatio = boxWidth / boxHeight;
        //Si se necesita mas ancho que el que hay disponible, redimensionamos las medidas respecto al ancho disponible
        if (boxHeight > height){
            boxHeight = height;
            boxWidth = boxHeight  * boxRatio;            
            circleSize = boxWidth / ((mTotalProgress + 1) * spaceRatio + mTotalProgress);
        }
        double x = (width - boxWidth) / 2;
        double y = (height - boxHeight) / 2;
        
        gc.save();
        gc.clearRect(0, 0, width, height);
       
        Font font = new Font("Arial", 16);     
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(mBackground);
        gc.setEffect(mInnerShadow);
        gc.fillRoundRect(x, y, boxWidth, boxHeight, circleSize / 2, circleSize / 2);
        
       
        
        gc.setFill(mFailBall);
        //Fallo por completo el enroll, dibujamos todos los circulos en rojo
        if (mStatus == 1){
            gc.setEffect(mDropShadow);
            for (int i = 0; i < mTotalProgress; i++){
                double nX = x + (i+1) * circleSize * spaceRatio + i* circleSize;
                double nY = y + circleSize * spaceRatio;
                gc.fillOval(nX, nY, circleSize, circleSize);      
            }
        }
        else{
            
            for (int i = 0; i < mProgress; i++){
                double nX = x + (i+1) * circleSize * spaceRatio + i* circleSize;
                double nY = y + circleSize * spaceRatio;
                gc.setEffect(mDropShadow);
                gc.setFill(mFilledBall);
                gc.fillOval(nX, nY, circleSize, circleSize);
                gc.setFill(mTextColor);
                gc.fillText("" + (1+i), nX + circleSize/2, nY + circleSize/2);               
                
                
                if (mThumbs != null && mThumbs[i]!=null){
                    gc.setEffect(mImageShadow);
                    nY = nY + circleSize + circleSize * spaceRatio;
                    gc.fillRect(nX-1, nY-1, circleSize + 2, circleSize + 2);
                    gc.drawImage(mThumbs[i], nX, nY, circleSize, circleSize);
                }
            }
          
            gc.setFill(mEmptyBall);
            gc.setEffect(mInnerShadow);
            for (int i = mProgress; i < mTotalProgress; i++){
                double nX = x + (i+1) * circleSize * spaceRatio + i* circleSize;
                double nY = y + circleSize * spaceRatio;
                if (mStatus == 2 && i == mProgress){
                    gc.setFill(mFailBall);
                }
                else{
                    gc.setFill(mEmptyBall);
                }
                gc.fillOval(nX, nY, circleSize, circleSize);
            }
           
        }
        
       
        gc.restore();
    }

    
}
