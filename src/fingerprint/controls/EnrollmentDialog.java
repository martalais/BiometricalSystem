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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

import uaz.fingerprint.Reader;

/**
 * FXML Controller class
 *
 * @author xmbeat
 */
public class EnrollmentDialog extends Stage implements Initializable {
    @FXML private VBox boxProgressContainer;
    @FXML private VBox boxImageContainer;
    @FXML private Label lblStatus;
    @FXML private ComboBox cmbDevices;
    private EnrollmentProgressViewer mEnrollmentViewer;
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
        Reader.init();
        List<Reader> list = Reader.listDevices();
       
        if (list != null){            
            ObservableList<Reader> devices = FXCollections.observableArrayList(list);
            cmbDevices.setItems(devices);
            cmbDevices.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
                if (newValue != null){
                   Reader reader = (Reader) newValue;
                   initReader(reader);
                }
            });
        }
        
        this.show();
        Reader.exit();
    }
    
    private void initReader(Reader reader){
        //Desabilitamos la seleccion de dispositvos mientras estemos usando el actual
        cmbDevices.setDisable(true);
        if (mEnrollmentViewer != null){
            mEnrollmentViewer.stop();
        }
        //Creamos el control asociado al reader
        mEnrollmentViewer = new EnrollmentProgressViewer(reader);
        //Agregamos el visor de los enrolls
        VBox.setVgrow(mEnrollmentViewer, Priority.ALWAYS);
        VBox.setMargin(mEnrollmentViewer, new Insets(20));
        boxProgressContainer.getChildren().remove(1);
        boxProgressContainer.getChildren().add(1, mEnrollmentViewer);
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //Colocamos un icono en un canvas que muestre una imagen representativa del proceso de enrollment
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
