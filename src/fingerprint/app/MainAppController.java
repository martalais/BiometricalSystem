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
import fingerprint.model.Permiso;
import fingerprint.webcam.WebcamDialog;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import uaz.fingerprint.EnrollResult;

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
    @FXML private TitledPane tpnFingers;
    @FXML private TitledPane tpnImage;
    @FXML private Accordion acnRegister;
    @FXML private VBox boxUserDataContainer;
    private CanvasImageViewer mCanvas;
    private EnrollResult[] mUserFingers;
    private Image mIconDelete;
    private Image mIconAdd;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Obtenemos las imagenes que necesitaremos desde los recursos
        mIconDelete = new Image(this.getClass().getResourceAsStream("/resources/remove.png"));
        mIconAdd = new Image(this.getClass().getResourceAsStream("/resources/add.png"));
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
        TableView<Permiso> tblPermisos = new TableView<>();
        tblPermisos.setEditable(true);
        ObservableList<Permiso> mData = FXCollections.observableArrayList();
        
        mData.add(new Permiso(0, "Acceso a laboratorio de redes"));
        mData.add(new Permiso(1, "Acceso a servidores"));
        mData.add(new Permiso(2, "Acceso a laboratorios de computación"));
        mData.add(new Permiso(3, "Acceso a aulas"));                 
        mData.add(new Permiso(4, "Acceso a sala de maestros"));
                
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
        tblPermisos.setItems(mData);
        VBox.setMargin(tblPermisos,new Insets(10, 0, 0, 0));
        boxUserDataContainer.getChildren().add(tblPermisos);
        acnRegister.setExpandedPane(tpnImage);
    }    
    
}
