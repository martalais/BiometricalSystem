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
import fingerprintlogin.webcam.WebcamStreamViewer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author xmbeat
 */
public class WebcamDialog extends Stage implements Initializable{
    @FXML private Button btnAccept;
    @FXML private Button btnCapture;
    @FXML private Button btnCancel;
    @FXML private ComboBox cmbDevices;
    @FXML private HBox boxStreamImage;
    private CanvasImageViewer mCanvas = new CanvasImageViewer();
    private ProgressIndicator mProgress = new ProgressIndicator(-1.0);
    
    private WebcamStreamViewer mStreamViewer;
    private BufferedImage mResultImage = null;
    


    public WebcamDialog(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WebcamDialog.fxml"));
        fxmlLoader.setController(this);
        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setTitle("Tome una fotografía");
    }
 
    /**
     * Muestra la ventana de captura de imagen de webcam
     * @param owner
     * @return Regresa la fotografia que ha capturado el usuario a traves de la webcam o null si cancela la operacion o si no tomó fotografia.
     */
    public BufferedImage showDialog(final Window owner){
        
        ObservableList<WebcamInfo> devices = FXCollections.observableArrayList();
        int counter = 0;
        for (Webcam webcam : Webcam.getWebcams()){
            WebcamInfo device = new WebcamInfo(webcam, counter++);    
            devices.add(device);
        }
        //Cuando se seleccione una webcam de la lista tratamos de abrirlo e iniciar el stream de la webcam
        cmbDevices.setItems(devices);
        cmbDevices.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            if (newValue != null){
                WebcamInfo device = (WebcamInfo) newValue;
                initWebcam(device.getWebcam());
            }
        });
        
        this.initModality(Modality.WINDOW_MODAL);
        this.initOwner(owner);        
        this.setResizable(false);
        showAndWait();
        return mResultImage;
    }
    
    private void initWebcam(Webcam webcam){
        //Deshabilitamos el combo para que no pueda seleccionar otros dispositivos mientras uno este cargando
        cmbDevices.setDisable(true);
        //Agregamos el progress indicator con el cual darmemos a entender al usuario que se está accediendo al webcam
        boxStreamImage.getChildren().clear();  
        HBox.setHgrow(mProgress, Priority.ALWAYS);
        HBox.setMargin(mProgress, new Insets(50));
        boxStreamImage.getChildren().add(mProgress);
        //Abrimos un stream a la webcam donde se reflejara en el canvas
        if (mStreamViewer != null){
            mStreamViewer.stop();
        }
        mStreamViewer = new WebcamStreamViewer(webcam, mCanvas, new WebcamStreamViewer.WebcamStreamListener() {
            @Override
            public void onStart(Webcam webcam) {
               cmbDevices.setDisable(false);
               boxStreamImage.getChildren().clear();
               HBox.setHgrow(mCanvas, Priority.ALWAYS);
               boxStreamImage.getChildren().add(mCanvas);
               
            }

            @Override
            public void onStop(Webcam webcam) {

            }
        }); 
        mStreamViewer.start();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        btnAccept.setOnAction((event) -> {
            if (mStreamViewer != null){
                mStreamViewer.stop();
                mResultImage = mStreamViewer.getLastImage();
            }
            close();
        });
        btnCancel.setOnAction((event) -> {
            if (mStreamViewer != null){
                mStreamViewer.stop();
            }
            mResultImage = null;
            close();
        });
        btnCapture.setOnAction((event) -> {
            if (mStreamViewer != null){
                if (mStreamViewer.isPaused()){
                    mStreamViewer.play();
                    btnCapture.setText("Capturar");
                }
                else{
                    mStreamViewer.pause();
                    btnCapture.setText("Tomar otra");
                }
                
            }
        });
        this.setOnCloseRequest((event)->{
            event.consume();
            btnCancel.fire();
        });
    }
    
    
    private class WebcamInfo {
        private Webcam mDevice;
        private int mIndex;
        
        public WebcamInfo(Webcam device, int index){
            mIndex = index;
            mDevice = device;
        }
        
        public Webcam getWebcam(){
            return mDevice;
        }
        
        public String getName() {
                return mDevice.getName();
        }


        public int getIndex() {
                return mIndex;
        }

        public void setIndex(int webCamIndex) {
                this.mIndex = webCamIndex;
        }

        @Override
        public String toString() {
                return getName();
        }
    }

}
