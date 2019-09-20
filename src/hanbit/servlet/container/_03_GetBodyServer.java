package hanbit.servlet.container;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class _03_GetBodyServer {
    private static final byte CR = '\r';
    private static final byte LF = '\n';

    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public static void main(String[] args) throws IOException {
        _03_GetBodyServer server = new _03_GetBodyServer();
        server.boot();
    }

    private void boot() throws IOException {
        openSocketAndStream(8000);
        printRequest();
        closeStreamAndSocket();
    }

    private void printRequest() throws IOException {
        boolean bodyFlag = false;
        String method = null;
        String requestUrl = null;
        String httpVersion = null;
        int contentLength = -1;
        int bodyRead = 0;
        List<Byte> bodyBytes = null;
        Map<String, String> headers = new HashMap<>();


        int oneInt = -1;
        byte oldByte = (byte)-1;
        StringBuilder sb = new StringBuilder();
        int lineNumber = 0;

        while (-1 != (oneInt = inputStream.read())) {
            byte currentByte = (byte)oneInt;

            if (bodyFlag) {
                bodyRead++;
                bodyBytes.add(currentByte);

                if (bodyRead >= contentLength) {
                    break;
                }
            }
            else {
                if (currentByte == _03_GetBodyServer.LF && oldByte == _03_GetBodyServer.CR) {
                    String oneLine = sb.substring(0, sb.length() - 1);
                    lineNumber++;

                    if (lineNumber == 1) {
                        int firstBlank = oneLine.indexOf(" ");
                        int secondBlank = oneLine.lastIndexOf(" ");
                        method = oneLine.substring(0, firstBlank);
                        requestUrl = oneLine.substring(firstBlank + 1, secondBlank);
                        httpVersion = oneLine.substring(secondBlank + 1);
                    } else if (oneLine.length() <= 0) {
                        bodyFlag = true;

                        if ("GET".equals(method)) {
                            break;
                        }

                        String contentLengthValue = headers.get("Content-Length");

                        if (contentLengthValue != null) {
                            contentLength = Integer.parseInt(contentLengthValue.trim());
                            bodyFlag = true;
                            bodyBytes = new ArrayList<>();
                        }
                        continue;

                        int indexOfColon = oneLine.indexOf(":");
                        String headerName = oneLine.substring(0, indexOfColon);
                        String headerValue = oneLine.substring(indexOfColon);

                        headers.put(headerName, headerValue);
                    }
                    sb.setLength(0);
                }
                else {
                    sb.append((char) currentByte);
                }
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