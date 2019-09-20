package hanbit.servlet.container;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class _02_GetHeaderServer {
    private static final byte CR = '\r';
    private static final byte LF = '\n';

    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public static void main(String[] args) throws IOException {
        _02_GetHeaderServer server = new _02_GetHeaderServer();
        server.boot();
    }

    private void boot() throws IOException {
        openSocketAndStream(8000);
        printRequest();
        closeStreamAndSocket();
    }

    private void printRequest() throws IOException {
        int oneInt = -1;
        byte oldByte = (byte)-1;
        StringBuilder sb = new StringBuilder();
        int lineNumber = 0;

        while (-1 != (oneInt = inputStream.read())) {
            byte thisByte = (byte)oneInt;

            if (thisByte == _02_GetHeaderServer.LF && oldByte == _02_GetHeaderServer.CR) {
                String oneLine = sb.substring(0, sb.length()-1);
                lineNumber++;
                System.out.printf("%d: %s\n", lineNumber, oneLine);

                if (oneLine.length() <= 0) {
                    System.out.println("[SYS] 내용이 없는 헤더, 즉, 메시지 헤더의 끝");
                    break;
                }

                sb.setLength(0);
            }
            else {
                sb.append((char)thisByte);
            }

            oldByte = (byte)oneInt;
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