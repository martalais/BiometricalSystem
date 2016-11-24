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
package fingerprint.app;

import fingerprint.controls.EnrollmentDialog;
import fingerprint.webcam.WebcamDialog;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/**
 * FXML Controller class
 *
 * @author xmbeat
 */
public class MainAppController implements Initializable {
    @FXML private Button btnBrowseImage;
    @FXML private Button btnWebcam;
    @FXML private VBox boxImageContainer;
    @FXML private Button btnRegister;
    private CanvasImageViewer mCanvas;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Agregamos el canvas donde pintaremos la imagen que seleccione el usuario
        mCanvas = new CanvasImageViewer();
        boxImageContainer.getChildren().remove(0);
        VBox.setVgrow(mCanvas, Priority.ALWAYS);
        boxImageContainer.getChildren().add(0, mCanvas);
        
        //Accion cuando se seleccione la imagen desde archivo
        btnBrowseImage.setOnAction((event) -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.bmp"));
         
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File file = chooser.showOpenDialog(btnBrowseImage.getScene().getWindow());
            if (file != null){
                try {
                    BufferedImage image = ImageIO.read(file);
                    mCanvas.setImage(image);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        //Accion cuando se seleccione desde la webcam
        btnWebcam.setOnAction((event) -> {
            WebcamDialog webcam = new WebcamDialog();
            BufferedImage image =  webcam.showDialog(btnWebcam.getScene().getWindow());
            if (image != null){
                mCanvas.setImage(image);
            }
        });
        
        btnRegister.setOnAction((event) -> {
            EnrollmentDialog enrollDialog = new EnrollmentDialog();
            enrollDialog.showDialog();
        });
    }    
    
}
