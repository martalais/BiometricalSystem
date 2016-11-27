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
import javafx.application.Platform;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import uaz.fingerprint.EnrollResult;

import uaz.fingerprint.Reader;
import uaz.fingerprint.ReaderListener;

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
    
    public void showDialog(Window parent){
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
        this.initOwner(parent);
        this.initModality(Modality.WINDOW_MODAL);
        this.showAndWait();
    }
    
    private void setStatus(String message, int state){
        lblStatus.setText(message);
    }
    private void initReader(Reader reader){
        //Desabilitamos la seleccion de dispositvos mientras estemos usando el actual
        cmbDevices.setDisable(true);
        if (mEnrollmentViewer != null){
            mEnrollmentViewer.getReader().stop();
        }
       
         
        //Creamos el control asociado al reader
        mEnrollmentViewer = new EnrollmentProgressViewer(reader);
        reader.addListener(new ReaderListener(){
            @Override
            public void onStartCapture(Reader reader) {
                System.out.println("Reader start()");
                Platform.runLater(() -> {
                    setStatus("El scaner esta listo para procesar su huella", 0);
                });
            }
                    
            @Override
            public void onCapture(Reader reader, EnrollResult result) {
                if (result.getCode() == EnrollResult.COMPLETE){
                
                }
                if (result.getCode() == EnrollResult.PASS){
                    Platform.runLater(() -> {
                        setStatus("El escaneo fue exitoso, coloca tu dedo en el lector otra vez", 1);
                    });
                }
                else if (result.getCode() == EnrollResult.FAIL){
                    Platform.runLater(() -> {
                        setStatus("El escaneo falló por completo, reinicie el proceso de escaneo por favor", 2);
                    });
                }
                else{
                    
                }
            }

            @Override
            public void onClose(Reader reader) {
                
            }

            @Override
            public void onStopCapture(Reader reader) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onOpen(Reader reader) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        reader.start();
        //Agregamos el visor de los enrolls
        VBox.setVgrow(mEnrollmentViewer, Priority.ALWAYS);
        VBox.setMargin(mEnrollmentViewer, new Insets(20));
        boxProgressContainer.getChildren().remove(2);
        boxProgressContainer.getChildren().add(2, mEnrollmentViewer);
        
        this.setOnCloseRequest((event) -> {
            mEnrollmentViewer.getReader().stop();
        });
        
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
        
    }    
    
}
