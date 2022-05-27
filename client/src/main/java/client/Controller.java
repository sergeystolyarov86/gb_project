package client;


import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Controller {

    @FXML
    public MenuBar menuBar;

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public Button sendOnServer;

    @FXML
    VBox clientPanel, serverPanel;


    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String nickname;

    private Stage regStage;
    private RegController regcontroller;


    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        if (!authenticated) {
            nickname = "";
        }
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith("/auth_ok")) {
                                nickname = str.split("\\s+")[1];
                                setAuthenticated(true);
                                break;
                            }
                            if (str.startsWith("/reg_ok")) {
                                regcontroller.showResult("/reg_ok");
                            }
                            if (str.startsWith("/reg_no")) {
                                regcontroller.showResult("/reg_no");
                            }
                        }
                    }
                    while (authenticated) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            break;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("disconnect");
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void tryToAuth() {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/auth %s %s", loginField.getText().trim(), passwordField.getText().trim());
        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("ChatGB registration");
            regStage.setScene(new Scene(root, 400, 320));

            regcontroller = fxmlLoader.getController();
            regcontroller.setController(this);

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg() {
        if (regStage == null) {
            createRegWindow();
        }
        Platform.runLater(() -> regStage.show());
    }

    public void registration(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyBtnAction(ActionEvent actionEvent) {
        ClientController clientPC = (ClientController) clientPanel.getProperties().get("ctrl");
        ClientController serverPC = (ClientController) serverPanel.getProperties().get("ctrl");

        if (clientPC.getSelectedFileName() == null && serverPC.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "не был выбран файл", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        ClientController srcPc = null, dstPC = null;
        if(clientPC.getSelectedFileName()!=null){
            srcPc = clientPC;
            dstPC = serverPC;
        }
        if(serverPC.getSelectedFileName()!=null){
            srcPc = serverPC;
            dstPC = clientPC;
        }
        Path srcPath = Paths.get(srcPc.getCurrentPath(),srcPc.getSelectedFileName());
        Path dstPAth = Paths.get(dstPC.getCurrentPath()).resolve(srcPath.getFileName().toString());

        try {
            Files.copy(srcPath,dstPAth);
            dstPC.updateList(Paths.get(dstPC.getCurrentPath()));
        } catch (IOException e) {
           Alert alert = new Alert(Alert.AlertType.ERROR,"не удалось скопировать файл",ButtonType.OK);
           alert.showAndWait();
        }
    }
}



