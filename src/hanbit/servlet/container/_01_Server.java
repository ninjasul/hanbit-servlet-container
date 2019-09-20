package hanbit.servlet.container;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class _01_Server {
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public static void main(String[] args) throws IOException {
        _01_Server server = new _01_Server();
        server.boot();
    }

    private void boot() throws IOException {
        openSocketAndStream(8000);
        printRequest();
        closeStreamAndSocket();
    }

    private void printRequest() throws IOException {
        int oneInt = -1;

        while (-1 != (oneInt = inputStream.read())) {
            System.out.print((char)oneInt);
        }
    }

    private void openSocketAndStream(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        socket = serverSocket.accept();
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    private void closeStreamAndSocket() throws IOException {
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}