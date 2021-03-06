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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


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
    public Button sendOnClient;

    @FXML
    VBox clientPanel, serverPanel;

    private static final String FILE_PARAMS_TPL = "FILE_NAME=%s&USERNAME=Bogdan&PASS=qwerty ";
    public static final String PATH_TO_CLIENT_DIR = "C:/";


    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated ;
    private String nickname;
    private SocketChannel socketChannel;
    private Stage regStage;
    private RegController regcontroller;
    private ByteBuffer buffer;


    public void setAuthenticated(boolean authenticated) {

            this.authenticated = authenticated;
            authPanel.setVisible(!authenticated);
            authPanel.setManaged(!authenticated);
            clientPanel.setVisible(authenticated);
            serverPanel.setVisible(authenticated);
            sendOnServer.setVisible(authenticated);
            sendOnClient.setVisible(authenticated);

    }

    void connect() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(IP_ADDRESS, PORT));
            socketChannel.configureBlocking(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void tryToAuth() {
        if (socketChannel == null || !socketChannel.isConnected()) {
            connect();
        }
        String msg = String.format("/auth %s %s ", loginField.getText().trim(), passwordField.getText().trim());

        buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));

        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.clear();
        listen();
        passwordField.clear();
    }

    private void listen() {

        try {
            int readBytes;
            while ((readBytes = socketChannel.read(buffer)) > 0) {
                buffer.flip();
                System.out.println(new String(buffer.array(), StandardCharsets.UTF_8));
                buffer.clear();
            }

            String str = new String(buffer.array(), StandardCharsets.UTF_8);

            if (str.startsWith("/auth_ok")) {
                nickname = str.split("\\s+")[1];
                setAuthenticated(true);
            }
            if (str.startsWith("/reg_ok")) {
                regcontroller.showResult("/reg_ok");
            }
            if (str.startsWith("/reg_no")) {
                regcontroller.showResult("/reg_no");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Net storage registration");
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
//
//    public void registration(String login, String password, String nickname) {
//        if (socket == null || socket.isClosed()) {
//            connect();
//        }
//        String msg = String.format("/reg %s %s %s", login, password, nickname);
//        try {
//            out.writeUTF(msg);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    public void copyBtnAction(ActionEvent actionEvent) {

//        ClientController clientPC = (ClientController) clientPanel.getProperties().get("ctrl");
//        ClientController serverPC = (ClientController) serverPanel.getProperties().get("ctrl");
//
//        if (clientPC.getSelectedFileName() == null && serverPC.getSelectedFileName() == null) {
//            Alert alert = new Alert(Alert.AlertType.ERROR, "???? ?????? ???????????? ????????", ButtonType.OK);
//            alert.showAndWait();
//            return;
//        }
//        ClientController srcPc = null, dstPC = null;
//        if(clientPC.getSelectedFileName()!=null){
//            srcPc = clientPC;
//            dstPC = serverPC;
//        }
//        if(serverPC.getSelectedFileName()!=null){
//            srcPc = serverPC;
//            dstPC = clientPC;
//        }
//        Path srcPath = Paths.get(srcPc.getCurrentPath(),srcPc.getSelectedFileName());
//        Path dstPAth = Paths.get(dstPC.getCurrentPath()).resolve(srcPath.getFileName().toString());
//
//        try {
//            Files.copy(srcPath,dstPAth);
//            dstPC.updateList(Paths.get(dstPC.getCurrentPath()));
//        } catch (IOException e) {
//           Alert alert = new Alert(Alert.AlertType.ERROR,"???? ?????????????? ?????????????????????? ????????",ButtonType.OK);
//           alert.showAndWait();
//        }
    }
}

//    void sendFileToServerWithNio() throws IOException {
//        SocketChannel socketChannel = SocketChannel.open();
//        socketChannel.connect(new InetSocketAddress("localhost", 45001));
//        socketChannel.configureBlocking(false);
//
//        Selector selector = Selector.open();
//        socketChannel.register(selector, SelectionKey.OP_WRITE);
//
//        selector.select();
//
//        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
//        RandomAccessFile fileForSend = new RandomAccessFile(PATH_TO_CLIENT_DIR + "cat1.png", "rw");
//        FileChannel fileChannel = fileForSend.getChannel();
//
//        for (SelectionKey selectionKey : selector.selectedKeys()) {
//            if (selectionKey.isValid() && selectionKey.isWritable()) {
//                while (fileChannel.read(byteBuffer) != -1) {
//                    byteBuffer.flip();
//                    socketChannel.write(byteBuffer);
//                    byteBuffer.clear();
//                }
//            }
//        }


