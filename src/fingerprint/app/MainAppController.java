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
import fingerprint.dao.DAOPermiso;
import fingerprint.dao.DAOUsuario;
import fingerprint.model.Permiso;
import fingerprint.model.Usuario;
import fingerprint.webcam.WebcamDialog;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import uaz.fingerprint.CaptureListener;
import uaz.fingerprint.EnrollResult;
import uaz.fingerprint.Reader;
import uaz.fingerprint.ReaderException;
import uaz.fingerprint.ReaderListener;

/**
 * FXML Controller class
 *
 * @author xmbeat
 */
public class MainAppController implements Initializable {
    @FXML private Button btnBrowseImage;
    @FXML private Button btnWebcam;
    @FXML private VBox boxImageContainer;
    @FXML private VBox boxUserInfo;
    @FXML private VBox boxUserImage;
    @FXML private Button btnSave;
    @FXML private Button btnDevices;
    @FXML private Button btnAction;
    @FXML private TitledPane tpnFingers;
    @FXML private TitledPane tpnImage;
    @FXML private Accordion acnRegister;
    @FXML private VBox boxUserDataContainer;
    @FXML private TabPane tpnMain;
    @FXML private Tab tabAdmin;
    @FXML private Tab tabIdentification;
    @FXML private TabPane tpnAdmin;
    @FXML private ComboBox cmbDevices;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtMatricula;
    @FXML private Label lblStatus;
    @FXML private ProgressIndicator pinProgress;
    @FXML private DatePicker dprRegistroNacimiento;
    @FXML private TextField txtRegistroNombre;
    @FXML private TextField txtRegistroApellidos;
    @FXML private TextField txtRegistroEmail;
    @FXML private TextField txtRegistroMatricula;
    @FXML private Label lblRegistroStatus;
    
    private TableView<Permiso> tblPermisos ;
    private CanvasImageViewer mCanvasFinger;
    private CanvasImageViewer mCanvasImage;
    private CanvasImageViewer mCanvas;
    private EnrollResult[] mUserFingers;
    private Image mIconDelete;
    private Image mIconAdd;
    private Image mIconRefresh;
    private Image mIconCancel;
    private Image mIconUser;
    private Image mIconFingerprint;
    private FingerprintListener mController;
    private ArrayList<Permiso> mPermisos;
    private List<Reader> mDevices;
    private UserFinder mFinder = new UserFinder();
    private UserSaver mSaver = new UserSaver();
    private Reader mSelectedReader;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Obtenemos las imagenes que necesitaremos desde los recursos
        mIconDelete = new Image(this.getClass().getResourceAsStream("/resources/remove.png"));
        mIconAdd = new Image(this.getClass().getResourceAsStream("/resources/add.png"));
        mIconRefresh = new Image(this.getClass().getResourceAsStream("/resources/refresh.png"));
        mIconCancel = new Image(this.getClass().getResourceAsStream("/resources/cancel.png"));
        mIconUser = new Image(this.getClass().getResourceAsStream("/resources/user.png"));
        mIconFingerprint = new Image(this.getClass().getResourceAsStream("/resources/fingerprint.png"));
        //Agregamos el canvas donde pintaremos la imagen que seleccione el usuario
        mCanvas = new CanvasImageViewer();
        boxImageContainer.getChildren().remove(0);
        VBox.setVgrow(mCanvas, Priority.ALWAYS);
        boxImageContainer.getChildren().add(0, mCanvas);
        
        //Agregamos los botones para registrar las huellas
        String texts[] = {"Pulgar Derecho", "Índice Derecho", "Medio Derecho", "Anular Derecho", "Meñique Derecho", 
            "Pulgar Izquierdo", "Índice Izquierdo", "Medio Izquierdo", "Anular Izquierdo", "Meñique Izquierdo"};
        mUserFingers = new EnrollResult[texts.length];
        GridPane grid = new GridPane();
        ColumnConstraints c1 = new ColumnConstraints(120);
        ColumnConstraints c2 = new ColumnConstraints(80, 80, 10000, Priority.ALWAYS, HPos.CENTER, true);
        c1.setPercentWidth(50);
        EventHandler<ActionEvent> enrollHandler = (ActionEvent event) -> {
            Button btnCaller = (Button) event.getSource();
            Integer index = (Integer)btnCaller.getUserData();            
            if (btnCaller.getText().equalsIgnoreCase("Eliminar")){
                mUserFingers[index] = null;
                btnCaller.setText("Capturar");
                btnCaller.setGraphic(new ImageView(mIconAdd));
            }
            else{
                EnrollmentDialog dialog = new EnrollmentDialog();
                EnrollResult result = dialog.showDialog(btnCaller.getScene().getWindow());
                if (result != null){
                    mUserFingers[index] = result;
                    btnCaller.setText("Eliminar");
                    btnCaller.setGraphic(new ImageView(mIconDelete));
                }
            }
        };
        //Agregamos los botones de enroll
        grid.getColumnConstraints().addAll(c1, c2);
        for (int i = 0; i < texts.length; i++){
            Label lbl = new Label(texts[i]);
            Button btn = new Button("Capturar", new ImageView(mIconAdd));
            btn.setUserData(new Integer(i));
            btn.setMaxWidth(10000);
            btn.setOnAction(enrollHandler);
            grid.add(lbl, 0, i);            
            grid.add(btn, 1, i);
        }
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        tpnFingers.setContent(grid);
        
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
        
        tpnMain.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> ov, Tab previousTab, Tab selectedTab) {
                    if (selectedTab == tabAdmin){
                        if (mPermisos == null){
                            cmbDevices.setItems(null);
                            tpnAdmin.setDisable(true);
                            DataLoader loader = new DataLoader();
                            loader.start();
                        }
                        mFinder.cancel();
                        if (mDevices != null){
                            for (Reader reader: mDevices){
                                reader.close();
                            }
                        }
                    }
                }
            }
        );


        
        tblPermisos = new TableView<>();
        tblPermisos.setEditable(true);
        
                
        TableColumn colEnabler = new TableColumn("Habilitar");
        TableColumn colDescription = new TableColumn("Descripción");
        
        colEnabler.setMinWidth(100);
        colDescription.setMinWidth(200);
        //colEnabler.setCellValueFactory(new PropertyValueFactory<Permiso, Boolean>("enabled"));
        colEnabler.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Permiso, Boolean>, ObservableValue<Boolean>> (){
            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Permiso, Boolean> param) {
                return param.getValue().enabledProperty();
            }
            
        });
        colEnabler.setCellFactory(CheckBoxTableCell.forTableColumn(colEnabler));
        colEnabler.setEditable(true);
        colDescription.setCellValueFactory(new PropertyValueFactory<Permiso, String>("description"));
        tblPermisos.getColumns().addAll(colEnabler, colDescription);
        
        VBox.setMargin(tblPermisos,new Insets(10, 0, 0, 0));
        boxUserDataContainer.getChildren().add(tblPermisos);
        acnRegister.setExpandedPane(tpnImage);
        
        //Agregando el boton que consigue la lista de dispositivos
        btnDevices.setGraphic(new ImageView(mIconRefresh));
        btnDevices.setOnAction((ActionEvent event) -> {
            btnDevices.setDisable(true);
            System.out.println("Getting devices...");
            refreshDevices();
        });
        mController = new FingerprintListener();
        
        //Agregando los contenedores para la huella y la imagen
        mCanvasFinger = new CanvasImageViewer();
        mCanvasFinger.setDefaultImage(mIconFingerprint);
        VBox.setVgrow(mCanvasFinger, Priority.ALWAYS);
        boxUserInfo.getChildren().add(mCanvasFinger);
        
        mCanvasImage = new CanvasImageViewer();
        mCanvasImage.setDefaultImage(mIconUser);
        VBox.setVgrow(mCanvasImage, Priority.ALWAYS);
        boxUserImage.getChildren().add(mCanvasImage);
        
        //Accion del boton cancelar busqueda
        btnAction.setOnAction((event) -> {
            if (btnAction.getText().equalsIgnoreCase("cancelar")){                
                mFinder.cancel();
            }
            else{
                try {
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec("firefox -new-window http://google.com");
                    System.out.println("Accedido!");
                } catch (Exception ex){
                }
            }
            if (mSelectedReader != null)
                mSelectedReader.startCapture();
        });
        
        //Accion del boton guardar usuario
        btnSave.setOnAction((event) -> {
            mSaver.start();
        });
    }
    
    private void refreshDevices(){
        Thread thread = new Thread(()->{
            if (mDevices != null){
                for (Reader reader: mDevices){
                    reader.stopCapture();
                    reader.close();
                }
            }
            mDevices = Reader.listDevices();
            if (mDevices != null){
                for (int i = 0; i < mDevices.size();){
                    try {
                        mDevices.get(i).open();
                        System.out.println(mDevices.get(i).getDriverName());
                        i++;
                    }
                    catch(ReaderException exc) {
                        mDevices.remove(i);
                    }
                }
            }
            
            Platform.runLater(()->{
                ObservableList<Reader> list = FXCollections.observableArrayList(mDevices);
                cmbDevices.setItems(list);
                cmbDevices.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
                    if (newValue != null){
                       Reader reader = (Reader) newValue;
                       mSelectedReader = reader;
                       reader.addListener(mController);
                       reader.startCapture();
                       
                    }
                    if (oldValue != null){
                        Reader reader = (Reader) oldValue;
                        reader.stopCapture();
                    } 
                });
                btnDevices.setDisable(false);
            });
            
        });
        thread.start();
        
    }
    private class DataLoader extends Thread{
        public void run(){
            try {
                DAOPermiso permisos = new DAOPermiso();
                mPermisos = permisos.getPermisos();
                ObservableList<Permiso> data = FXCollections.observableArrayList(mPermisos);
                
                tblPermisos.setItems(data);
                
            } catch (SQLException ex) {
                Logger.getLogger(MainAppController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MainAppController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Platform.runLater(() -> {            
                tpnAdmin.setDisable(false);
            });
        }
    }
    private class UserSaver implements Runnable{
        private Usuario mUsuario;
        private int status;
        public void start(){
            btnSave.setDisable(true);
            mUsuario = new Usuario();
            mUsuario.setApellidos(txtRegistroApellidos.getText());
            mUsuario.setEmail(txtRegistroEmail.getText());
            mUsuario.setNombre(txtRegistroNombre.getText());
            LocalDate localDate = dprRegistroNacimiento.getValue();
            if (localDate!= null){
                Calendar c = Calendar.getInstance();
                c.set(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
                mUsuario.setNacimiento(c.getTime());
            }
            BufferedImage img = mCanvas.getBufferedImage();
            if (img!= null){
                
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    ImageIO.write(img, "png", stream);
                    mUsuario.setImage(stream.toByteArray());
                } catch (IOException ex) {
                    Logger.getLogger(MainAppController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ObservableList<Permiso> permisos = tblPermisos.getItems();
            for (Permiso permiso: permisos){
                if (permiso.getEnabled()){
                    mUsuario.addPermiso(permiso);
                }
            }
            for (EnrollResult finger: mUserFingers){
                if (finger != null){
                    mUsuario.addFingerprint(finger);
                }
            }
            Thread thread = new Thread(this);
            thread.start();
        }
        public void run(){
            status = -1;
            try {
                DAOUsuario dao = new DAOUsuario();
                status = dao.add(mUsuario);
                Thread.sleep(2000);
            } catch (SQLException ex) {
                Logger.getLogger(MainAppController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MainAppController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainAppController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Platform.runLater(()->{
                btnSave.setDisable(false);
                txtRegistroApellidos.clear();
                txtRegistroEmail.clear();
                txtRegistroMatricula.clear();
                txtRegistroNombre.clear();
                if (status >= 0){
                    lblRegistroStatus.setText("Usuario agregado con éxito");
                }
                else{
                    lblRegistroStatus.setText("No se pudo agregar el usuario");
                }
            });
        }
    }
    private class UserFinder implements Runnable{
        private EnrollResult mFinger;
        private Reader mReader;
        private boolean mCancel;
        public void setReader(Reader reader){
            mReader = reader;
        }
        public void setMatchingFinger(EnrollResult finger){
            mFinger = finger;
        }
        public void  cancel(){
            mCancel = true;
            cmbDevices.setDisable(false);
            btnDevices.setDisable(false);
            lblStatus.setText("");
            pinProgress.setVisible(false);
            btnAction.setVisible(false);
        }
        public void start(){
            Thread thread = new Thread(this);
            mCancel = false;
            cmbDevices.setDisable(true);
            btnDevices.setDisable(true);
            lblStatus.setText("Buscando huella...");
            pinProgress.setVisible(true);
            btnAction.setText("Cancelar");
            btnAction.setVisible(true);
            thread.start();
            
        }
        public void run(){
            try {
                DAOUsuario dao = new DAOUsuario();
                Usuario usuario = dao.findByFingerprint(mFinger);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainAppController.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (mCancel == false){
                    Platform.runLater(()->{
                        if (usuario != null){
                            btnAction.setText("Acceder");
                            lblStatus.setText("Usuario encontrado!");
                            txtNombre.setText(usuario.getNombre());
                            txtApellidos.setText(usuario.getApellidos());
                            txtMatricula.setText(usuario.getMatricula());
                            if (usuario.getImage()!=null){
                                try {
                                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(usuario.getImage()));
                                    mCanvasImage.setImage(img, true);
                                } catch (IOException ex) {
                                    mCanvasImage.setImage((BufferedImage)null, true);
                                }
                            }
                            else{
                                mCanvasImage.setImage((BufferedImage)null, true);
                            }
                        }
                        else{
                            btnAction.setVisible(false);
                            lblStatus.setText("Usuario no encontrado!");                            
                            txtNombre.clear();
                            txtApellidos.clear();
                            txtMatricula.clear();
                            mCanvasImage.setImage(null);
                        }
                        pinProgress.setVisible(false);
                        cmbDevices.setDisable(false);
                        btnDevices.setDisable(false);
                        if (mReader != null)
                            mReader.startCapture();
                    });
                }
            } catch (SQLException ex) {
                Logger.getLogger(MainAppController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MainAppController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    private class FingerprintListener extends CaptureListener{

        @Override
        public void onCaptureStart(Reader reader) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void onCapture(Reader reader, EnrollResult result) {
            if (result.getCode() == EnrollResult.CAPTURE_COMPLETE){
                reader.stopCapture();
                Platform.runLater(()->{
                    mCanvasFinger.setImage(result.getImage().toBufferedImage(), true);
                    mCanvasImage.setImage((BufferedImage)null, true);                    
                    mFinder.setMatchingFinger(result);
                    mFinder.setReader(reader);
                    mFinder.start();
                });
                
                
            }
        }

        @Override
        public void onCaptureStop(Reader reader) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
}
