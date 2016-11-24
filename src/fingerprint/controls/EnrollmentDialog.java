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
package fingerprint.controls;

import fingerprint.app.CanvasImageViewer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * FXML Controller class
 *
 * @author xmbeat
 */
public class EnrollmentDialog extends Stage implements Initializable {
    @FXML private VBox boxProgressContainer;
    @FXML private VBox boxImageContainer;
    @FXML private Label lblStatus;
    private CanvasProgressViewer mProgressViewer;
    private CanvasImageViewer mImageViewer;
    
    
    public EnrollmentDialog(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EnrollmentDialog.fxml"));
        fxmlLoader.setController(this);
        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setTitle("Escaneo de huella digital");
    }
    
    public void showDialog(){
        this.show();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mProgressViewer = new CanvasProgressViewer(5);
        mProgressViewer.setProgress(2);
        
        VBox.setVgrow(mProgressViewer, Priority.ALWAYS);
        VBox.setMargin(mProgressViewer, new Insets(20));
        boxProgressContainer.getChildren().add(1, mProgressViewer);
        
        mImageViewer = new CanvasImageViewer();
        try {
            BufferedImage image = ImageIO.read(EnrollmentDialog.class.getResource("/resources/fingerprintgesture.png").openStream());
            mImageViewer.setDefaultImage(image);
            mImageViewer.setBackgroundColor(1, 1, 1, 0.5);
        } catch (Exception ex) {
            Logger.getLogger(EnrollmentDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        VBox.setVgrow(mImageViewer, Priority.ALWAYS);
        boxImageContainer.getChildren().add(mImageViewer);
        lblStatus.setText("Hola mundo");
    }    
    
}
