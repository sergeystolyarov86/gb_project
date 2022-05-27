package client;


import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ServerController implements Initializable {
    @FXML
    public HBox panel;

    @FXML
    public HBox authPanel;
    @FXML
    public VBox clientPanel;
    @FXML
    public ComboBox<String> disksBox;
    @FXML
    public TextField pathField;
    @FXML
    TableView<FileInfo> filesTable;


    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;



    private boolean authenticated;
    private String nickname;

   


    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        if (!authenticated) {
            nickname = "";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        filesTable.setVisible(false);
//        panel.setVisible(false);
//        menuBar.setVisible(false);
//        sendOnServer.setVisible(false);
        TableColumn<FileInfo,String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(50);


        TableColumn<FileInfo,String> fileNameColumn = new TableColumn<>("Filename");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(240);


        filesTable.getColumns().addAll(fileTypeColumn,fileNameColumn);
        filesTable.getSortOrder().add(fileTypeColumn);

        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);
        filesTable.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2){
                Path path = Paths.get(pathField.getText())
                        .resolve(filesTable.getSelectionModel()
                                .getSelectedItem().getFilename());
                if(Files.isDirectory(path)){
                    updateList(path);
                }
            }
        });

        updateList(Paths.get(""));
    }

    public void updateList(Path path){

        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING,"не удалось обновить список файлов");
            alert.showAndWait();
        }

    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if(upperPath != null){
            updateList(upperPath);
        }
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox <String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }
    public String getSelectedFileName(){
        if(!filesTable.isFocused()){
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFilename();
    }
    public  String getCurrentPath(){
        return pathField.getText();
    }
}

