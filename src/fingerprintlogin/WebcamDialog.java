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
package fingerprintlogin;

import com.github.sarxos.webcam.Webcam;
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
    
    private WebcamStreamTask mStreamTask;
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
        //Abrimos el stream en otro hilo para que no bloquee el actual
        mStreamTask = new WebcamStreamTask(webcam, mCanvas);
        Thread initializer = new Thread(mStreamTask);
        initializer.setDaemon(true);
        initializer.start();
        
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        btnAccept.setOnAction((event) -> {
            close();
        });
        btnCancel.setOnAction((event) -> {
            mResultImage = null;
            close();
        });
        btnCapture.setOnAction((event) -> {
            initWebcam(null);
        });
    }
    
    private class WebcamStreamTask extends Task<Void>{
        private boolean mPlay;
        private BufferedImage mLastImage;
        private Webcam mWebcam;
        private CanvasImageViewer mCanvas;
        
        public WebcamStreamTask(Webcam webcam, CanvasImageViewer canvas){
            mPlay = true;
            mWebcam = webcam;
            mCanvas = canvas;
            
        }
        public void play(){
            mPlay = true;
        }
        public void stop(){
            mPlay = false;
        }
       
        @Override
        protected Void call() throws Exception {
            Runnable imageUpdater = () -> {
                mCanvas.updateDraw();
            };
            mWebcam.open();
            
            Platform.runLater(() -> {               
                boxStreamImage.getChildren().clear();
                boxStreamImage.getChildren().add(mCanvas);
            });
            
            while(isRunning()){
                if (mPlay){                    
                    mLastImage = mWebcam.getImage();
                    mCanvas.setImage(mLastImage, false);
                    Platform.runLater(imageUpdater);
                }
                else{
                    Thread.sleep(100);
                }
            }
            return null;
        }
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
