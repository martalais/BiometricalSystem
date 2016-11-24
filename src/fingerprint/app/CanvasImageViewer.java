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
package fingerprint.app;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 *
 * @author xmbeat
 */
public class CanvasImageViewer extends Canvas {
    private Color mBackground = new Color(0.2, 0.3, 0.3, 1);
    private Color cameraColor = new Color(0.8, 0.8, 0.8, 1.0);
    private Image mImage;
    private Image mDefaultImage;
    
    @Override
    public double minHeight(double width)
    {
        return 64;
    }

    public void setBackgroundColor(double r, double g, double b, double a){
        mBackground = new Color(r, g, b, a);
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
    

    public void setImage(BufferedImage img, boolean update){
        setImage(img);
        if (update)
            updateDraw();
    }
    
    public void setImage(BufferedImage img){
        if (img != null){
            mImage = SwingFXUtils.toFXImage(img, (WritableImage)mImage);
        }
        else{
            mImage = null;
        }
    }
    
    public void setDefaultImage(BufferedImage img){
        if (img != null){
            mDefaultImage = SwingFXUtils.toFXImage(img, (WritableImage)mDefaultImage);
        }
        else{
            mDefaultImage = null;
        }
    }
    
    public void updateDraw(){
        if (mImage == null){
            if (mDefaultImage == null){
                drawNoImage();
            }
            else{
                drawImage(mDefaultImage);
            }
        }
        else{
            drawImage(mImage);
        }
    }

    
    private void drawImage(Image img){
    
        GraphicsContext gc = getGraphicsContext2D();
        double width = getWidth();
        double height = getHeight();
        double ratio = height / width;
        double imageRatio = img.getHeight() / img.getWidth();
        double imageWidth;
        double imageHeight;
        //El largo de la imagen es mayor a la zona donde pintaremos relativamente
        if (imageRatio < ratio){
            //Escalamos la imagen respecto al largo de la imagen
            imageWidth = width;
            imageHeight = imageWidth * imageRatio;
        }
        else{
            //Escalamos la imagen respecto al ancho de la imagen
            imageHeight = height;
            imageWidth = imageHeight / imageRatio;
        }
        gc.clearRect(0, 0, width, height);  
        System.out.println("size: " + width + " " + height);
        gc.setFill(mBackground);
        gc.fillRect(0, 0, width, height);
        gc.drawImage(img, (width - imageWidth) / 2.0, (height-imageHeight) /2.0, imageWidth, imageHeight);
        
    }
    
    //Dibuja una camara
    private void drawNoImage(){
        double width = getWidth();
        double height = getHeight();

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        
        
        double ratio = 0.6; //Relacion del height respecto al width
        double margin = 30.0;
        double boxHeight = height * 0.35;        
        double boxWidth = boxHeight / ratio;
        double topBoxHeight = height * 0.10;        
        double topBoxWidth = boxWidth * 0.5;
        double arcSize;
        double cameraHeight;
        double circleSize;
      
        if (boxWidth > width - margin){
            boxWidth = width - margin;
            boxHeight = boxWidth * ratio;
            topBoxHeight = boxHeight * 0.10 / 0.35;
            topBoxWidth = boxWidth / 2.0;
        }
        arcSize = boxHeight / 3.0;
        cameraHeight = boxHeight + topBoxHeight / 2.0;
        circleSize = boxHeight * 0.7;
        //Dibujamos el fondo
        gc.setFill(mBackground);
        gc.fillRect(0, 0, width, height);
        
        //Dibujamos el cuerpo de la camara
        gc.setFill(cameraColor);
        gc.fillRoundRect((width - topBoxWidth) / 2.0, (height - cameraHeight) / 2.0, topBoxWidth, topBoxHeight, arcSize, arcSize);
        gc.fillRoundRect((width - boxWidth) / 2.0, (height - cameraHeight) / 2.0 + topBoxHeight * 0.5, boxWidth, boxHeight, arcSize, arcSize);
        //Dibujamos los circulos de la camara
        gc.setFill(mBackground);
        gc.fillOval((width - circleSize) / 2.0, (height - circleSize) / 2.0 + topBoxHeight * 0.25, circleSize, circleSize);
        circleSize *= 0.7;
        gc.setFill(cameraColor);
        gc.fillOval((width - circleSize) / 2.0, (height - circleSize) / 2.0 + topBoxHeight * 0.25, circleSize, circleSize);
        //Dibujamos el rectangulo del flash
        gc.setFill(mBackground);
        gc.fillRoundRect((width - boxWidth) / 2.0 + boxHeight * 0.1, (height - cameraHeight) / 2.0 + topBoxHeight * 0.5  + boxHeight * 0.1, boxWidth * 0.15, boxHeight * 0.15, arcSize / 2.0, arcSize / 2.0);
        
    }
}
