package server;

import auth.SimpleAuthService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NioServer {


    private static final Map<SocketChannel, FileChannel> socketFileChannel = new HashMap<>();
    private static final Map<SocketChannel, ConnectionMetadata> socketMetadataMap = new HashMap<>();
    private static final String SERVER_DIR = "C:";
    private Map<String, Boolean> authUsers = new HashMap<>();
    private static SimpleAuthService authService;
    private static ByteBuffer inboundBuffer = ByteBuffer.allocate(32);


    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(8189));
        serverChannel.configureBlocking(false);
        authService = new SimpleAuthService();

        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT); //ожидаем подключения

        log("Сервер стартовал на порту 8189. Ожидаем соединения...");

        while (true) {
            selector.select(); // Блокирующий вызов, только один
            for (SelectionKey event : selector.selectedKeys()) {
                if (event.isValid()) {
                    try {
                        if (event.isAcceptable()) { // Новое соединение
                            SocketChannel socketChannel = serverChannel.accept(); // Не блокирующий
                            socketChannel.configureBlocking(false);
                            log("Подключен " + socketChannel.getRemoteAddress());
                            socketChannel.register(selector, SelectionKey.OP_READ);
                        } else if (event.isReadable()) { // Готов к записи

                            SocketChannel socketChannel = (SocketChannel) event.channel();
                            auth(socketChannel);

                            //  handleReadable(socketChannel);

                        }
                    } catch (IOException e) {
                        log("ошибка " + e.getMessage());
                    }
                }
            }
            selector.selectedKeys().clear();
        }
    }


    private void isAuth(SocketChannel socketChannel) {

    }

    private static void auth(SocketChannel socketChannel) throws IOException {

        int readBytes;
        while ((readBytes = socketChannel.read(inboundBuffer)) > 0) {
            inboundBuffer.flip();
            inboundBuffer.clear();
        }

        String str = new String(inboundBuffer.array(), StandardCharsets.UTF_8);
        String[] authInfo = str.split(" ");
        if (authInfo[0].equals("/auth")) {

            String newNick = authService.getNicknameByLoginAndPassword(authInfo[1], authInfo[2]);

            if (newNick != null) {
                sendMsg("/auth_ok ", socketChannel);
                System.out.println("Client authenticated.\n" +
                        "Address: " + socketChannel.getRemoteAddress());

//                } else {
//                    sendMsg("Пользователь с таким логином уже авторизовался",socketChannel);
//                }
//            } else {
//                sendMsg("Неверный логин/пароль",socketChannel);
            }

            inboundBuffer.clear();
        }
    }


    private static void handleReadable(SocketChannel socketChannel) throws IOException {
        ConnectionMetadata connectionMetadata = socketMetadataMap.get(socketChannel);
        if (connectionMetadata == null) {
            connectionMetadata = new ConnectionMetadata();
            socketMetadataMap.put(socketChannel, connectionMetadata);
        }

        ByteBuffer inboundBuffer = ByteBuffer.allocate(4096);
        int readBytes;
        FileChannel fileChannel = null;
        while ((readBytes = socketChannel.read(inboundBuffer)) > 0) {
            inboundBuffer.flip();
            if (!connectionMetadata.isMetadataLoaded()) {
                loadMetadata(connectionMetadata, inboundBuffer);
            }
            if (inboundBuffer.hasRemaining()) {
                fileChannel = getFileChannel(socketChannel);
                fileChannel.write(inboundBuffer);
                inboundBuffer.clear();
            }
        }

        if (readBytes == -1) {
            if (fileChannel != null) {
                fileChannel.close();
            }
            socketChannel.close();
            socketFileChannel.remove(socketChannel);
            socketMetadataMap.remove(socketChannel);
        }
    }

    private static void loadMetadata(ConnectionMetadata connectionMetadata, ByteBuffer inboundBuffer) {
        while (inboundBuffer.hasRemaining()) { //если в буфере еще есть данные для чтения
            byte nextByte = inboundBuffer.get();
            if (nextByte == ' ') { // метаинформация закончилась, дальше файл
                connectionMetadata.buildMetadata();
                break;
            } else {
                connectionMetadata.getMetadataBuffer().put(nextByte);
            }
        }
    }

    private static FileChannel getFileChannel(SocketChannel socketChannel) throws FileNotFoundException {
        FileChannel fileChannel = socketFileChannel.get(socketChannel);
        ConnectionMetadata connectionMetadata = socketMetadataMap.get(socketChannel);
        if (fileChannel == null) {
            Map<String, String> metadataParams = connectionMetadata.getMetadataParams();
            String fileName = metadataParams.get("FILE_NAME");
            RandomAccessFile fileForSend = new RandomAccessFile(SERVER_DIR + fileName, "rw");
            fileChannel = fileForSend.getChannel();
            socketFileChannel.put(socketChannel, fileChannel);
        }
        return fileChannel;
    }

    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }

    private static void sendMsg(String message, SocketChannel socketChannel) {

        inboundBuffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));

        try {
            socketChannel.write(inboundBuffer);
             System.out.println(new String(inboundBuffer.array(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        inboundBuffer.clear();
    }
}
