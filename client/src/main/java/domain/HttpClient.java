package domain;

import status.Status;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class HttpClient {
    public static final long SELECT_INTERVAL = 200L;
    public static final int BUFFER_SIZE = 256;
    public static final byte CR = '\r';
    public static final byte LF = '\n';

    private static final ByteBuffer readBuffer = ByteBuffer.allocate(HttpClient.BUFFER_SIZE);

    private String host;
    private int port;
    private byte[] sendBytes;

    public HttpClient(String host, int port, String requestLine,
                      Map<String, String> headerMap, byte[] bodyBytes) {
        this.host = host;
        this.port = port;

        // 생성자에서는 전달받은 인자를 조합하여 HTTP 요청을 생성합니다.
        // request-line과 헤더를 \r\n을 사용해 나열합니다.
        StringBuilder sb = new StringBuilder();
        sb.append(requestLine).append("\r\n");

        Set<String> headerKeySet = headerMap.keySet();
        Iterator<String> headerKeyIter = headerKeySet.iterator();
        while (headerKeyIter.hasNext()) {
            String name = headerKeyIter.next();
            String value = headerMap.get(name);
            sb.append(name).append(": ").append(value).append("\r\n");
        }

        // 전송할 바디가 있다면 Content-Length 헤더에 바디의 크기를 값으로 추가합니다.
        if (bodyBytes != null) {
            sb.append("Content-Length:").append(bodyBytes.length).append("\r\n");
        }

        // 연결을 재사용하지 않음을 Connection 헤더 값에 close를 설정해 알려줍니다.
        // 만약 재사용할 예정이라면 서버가 요청을 임의로 끊지 말 것을 요청하는 의미로
        // close 대신 keep-alive를 전송합니다.
        sb.append("Connection: close\r\n");

        // 헤더부의 끝을 의미하기 위해 빈 라인 (\r\n\r\n)이 들어갔습니다.
        sb.append("\r\n");


        byte[] headerBytes = sb.toString().getBytes();

        // 전송할 내용을 의미하는 sendBytes에 지금까지 구성한 내용을 저장합니다.
        if (bodyBytes == null) {
            sendBytes = headerBytes;
        } else {
            sendBytes = new byte[headerBytes.length + bodyBytes.length];
            System.arraycopy(headerBytes, 0, sendBytes, 0, headerBytes.length);
            System.arraycopy(bodyBytes, 0, sendBytes, headerBytes.length,
                    bodyBytes.length);
        }
    }


    // 생성자에서 세팅된 값을 기반으로 실제 전송을 시도합니다.
    public void send() {
        SocketChannel socketChannel = null;
        Selector selector = null;
        boolean isEnd = false;
        try {

            // 먼저 소켓 채널을 생성한 후, 비동기 설정인 non-blocking을 추가합니다.
            socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false);

            // 앞서 보낼 내용을 지정한 sendBytes를 ByteBuffer 형태로 바꾼 후 전송합니다.
            socketChannel.write(ByteBuffer.wrap(sendBytes));

            // 이제 본격적으로 비동기적으로 읽기가 시작됩니다.
            // 셀렉터를 하나 열고, 읽기 이벤트가 발생하면 깨어나겠다는 것을 지정합니다.
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);
            while (!isEnd) {
                // 이제 SELECT_INTERVAL 시간 만큼 위에서 지정한 (READ) 이벤트를 기다립니다.
                selector.select(HttpClient.SELECT_INTERVAL);

                // 두 가지 경우 select 메서드가 반환됩니다.
                // 첫 번째는 대기하던 이벤트가 왔다면 반환됩니다.
                // 두 번째는 이벤트가 오지 않아도 지정한 타임아웃 시간(SELECT_INTERVAL)이
                // 지나면 반환됩니다.
                // 두 번째 경우에는 selectionKeys.iterator().hasNext()가 false이므로
                // 다시 위의 while 루프로 돌아가 다시 select 메서드에서
                // 이벤트를 대기하게 되겠습니다.
                Set<SelectionKey> selectionKeys = selector.keys();
                Iterator<SelectionKey> selectionKeyIter = selectionKeys.iterator();
                while (selectionKeyIter.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIter.next();

                    // 만약 지정된 이벤트가 들어왔다면 SelectionKey이 반환되게 되고\
                    // 이제 이벤트 타입 별로 각각의 처리가 시작됩니다.
                    // 위에서 READ 이벤트만 Selector에 등록했으므로
                    // 이벤트가 들어왔다면 아래 if 구문이 true가 됩니다.
                    if (selectionKey.isReadable()) {
                        // 이제 읽기 이벤트가 발생했으며 소켓 채널에서 실질적으로 읽게 됩니다.
                        // 문제는 읽어들였을 때 이번에 읽은 양이 전체 메시지라는 보장이 없습니다.
                        // 이론적으로 IP 패킷의 최대 크기는 65535입니다만
                        // 실질적으로는 그보다 훨씬 작은 크기가 사용됩니다.

                        // 따라서 많은 경우 하나의 HTTP 메시지는 여러 개의 패킷으로 이루어집니다.
                        // 먼저 소켓 채널에서 읽어 보고, 아직 메시지가 완성되지 않아
                        // 더 전달되어야 할 내용이 있다면 지금까지 읽은 내용을 저장하여 두고
                        // 다음 읽기 이벤트가 발생했을 때 다시 읽은 후
                        // 앞에서 저장된 내용과 합쳐 전체 메시지가 전달됐는지 확인하게 됩니다.
                        // 이를 위해 저장하여 두는 곳이 바로 SelectionKey.attachment가 되겠습니다.
                        MessageBag messageBag = (MessageBag) selectionKey.attachment();
                        if (messageBag == null) {
                            messageBag = new MessageBag();
                            selectionKey.attach(messageBag);
                        }

                        // 이제 읽기를 시도합니다.
                        // 읽기 버퍼를 초기화하고 읽습니다.
                        readBuffer.clear();
                        socketChannel.read(readBuffer);
                        readBuffer.flip();

                        while (readBuffer.hasRemaining()) {
                            // 읽어들인 내용을 앞에서부터 한 바이트씩 보고
                            // HTTP 메시지에서 현재 어떤 상태인지 확인하며 처리하게 됩니다.
                            byte oneByte = readBuffer.get();

                            // 처음 시작입니다. 상태를 요청 라인으로 바꿉니다.
                            if (messageBag.getStatus().equals(Status.INIT)) {
                                messageBag.add(oneByte);
                                messageBag.status = Status.REQUEST_LINE;

                                // 첫 줄인 요청 라인을 읽어들이다가 \r이 나오면 한 행이 끝남을,
                                // 곧 요청 라인이 종료되었음을 알 수 있습니다.
                            } else if (messageBag.getStatus().equals(Status.REQUEST_LINE)) {
                                if (oneByte == CR) {
                                    messageBag.status = Status.REQUEST_LINE_CR;
                                    messageBag.setRequestLine();
                                } else {
                                    messageBag.add(oneByte);
                                }
                            } else if (messageBag.getStatus().equals(Status.REQUEST_LINE_CR)) {
                                messageBag.status = Status.REQUEST_LINE_CRLF;
                            } else if (messageBag.getStatus().equals(Status.REQUEST_LINE_CRLF)) {
                                // 요청 라인이 종료된 이후에는 헤더로 간주합니다.
                                messageBag.add(oneByte);
                                messageBag.status = Status.HEADER;
                            } else if (messageBag.getStatus().equals(Status.HEADER)) {
                                if (oneByte == CR) {
                                    messageBag.addHeader();
                                    messageBag.status = Status.HEADER_CR;
                                } else {
                                    messageBag.add(oneByte);
                                }
                            } else if (messageBag.getStatus().equals(Status.HEADER_CR)) {
                                // 헤더 상태에서 \r을 만나면 하나의 헤더값이 완성되었다고 간주합니다.
                                if (oneByte == LF) {
                                    messageBag.status = Status.HEADER_CRLF;
                                } else {
                                    throw new IllegalStateException("LF must be followed.");
                                }
                            } else if (messageBag.getStatus().equals(Status.HEADER_CRLF)) {
                                if (oneByte == CR) {
                                    messageBag.status = Status.HEADER_CRLFCR;
                                } else {
                                    messageBag.add(oneByte);
                                    messageBag.status = Status.HEADER;
                                }
                            } else if (messageBag.getStatus().equals(Status.HEADER_CRLFCR)) {
                                // 빈 헤더값을 만나면 이제 헤더부가 끝났고
                                // 지금까지 들어온 요청 라인과 헤더를 파싱하여
                                // 메시지 바디 유무를 판단하여 더 필요하면 읽기를 계속합니다.
                                if (oneByte == LF) {
                                    BodyStyle bodyStyle = messageBag.afterHeader();
                                    if (bodyStyle == BodyStyle.NO_BODY) {
                                        messageBag.status = Status.TERMINATION;
                                        break;
                                    } else {
                                        messageBag.status = Status.BODY;
                                    }
                                } else {
                                    throw new IllegalStateException("LF must be followed.");
                                }
                            } else if (messageBag.getStatus().equals(Status.BODY)) {

                                // 메시지 바디가 있는 경우
                                if (messageBag.bodyStyle == BodyStyle.CONTENT_LENGTH) {

                                    // Content-Length 헤더가 있다면 해당 헤더 값만큼 바디를 추가로 읽습니다.
                                    messageBag.add(oneByte);
                                    if (messageBag.getContentLength() <= messageBag.getBytesSize()) {

                                        // 메시지 바디를 끝까지 다 읽었다면
                                        // 지금까지 읽은 값을 bodyBytes에 넣고, 상태를 종료로 표시하고 나갑니다.
                                        messageBag.setBodyBytes();
                                        messageBag.status = Status.TERMINATION;
                                        break;
                                    }
                                } else if (messageBag.bodyStyle == BodyStyle.CHUNKED) {
                                    // 메시지 청크 방식인 경우 HTTP 바디에 대해 다시 상태 기계를 만듭니다.
                                    if (messageBag.chunkStatus == ChunkStatus.CHUNK_NUM) {
                                        if (oneByte == CR) {
                                            messageBag.setChunkSize(Integer.parseInt(new String(messageBag.toBytes()), 16));
                                            messageBag.chunkStatus = ChunkStatus.CHUNK_NUM_CR;
                                        } else {
                                            messageBag.add(oneByte);
                                        }
                                    } else if (messageBag.chunkStatus == ChunkStatus.CHUNK_NUM_CR) {
                                        if (oneByte == LF) {
                                            if (messageBag.getChunkSize() == 0) {
                                                // 크기가 0인 청크는 청크 방식 메시지 바디의 끝을 의미합니다.
                                                // 메시지 상태를 종료로 표시하고 나갑니다.
                                                messageBag.setChunkBodyBytes();
                                                messageBag.status = Status.TERMINATION;
                                                break;
                                            } else {
                                                messageBag.chunkStatus = ChunkStatus.CHUNK_BODY;
                                            }
                                        } else {
                                            throw new IllegalStateException("LF must be followed by CR");
                                        }
                                    } else if (messageBag.chunkStatus == ChunkStatus.CHUNK_BODY) {
                                        if (messageBag.getBytesSize() == messageBag.getChunkSize() - 1) {
                                            messageBag.add(oneByte);
                                            messageBag.addChunk();
                                            messageBag.chunkStatus = ChunkStatus.CHUNK_END;
                                        } else {
                                            messageBag.add(oneByte);
                                        }
                                    } else if (messageBag.chunkStatus == ChunkStatus.CHUNK_END) {
                                        if (oneByte == CR) {
                                            messageBag.chunkStatus = ChunkStatus.CHUNK_CR;
                                        } else {
                                            throw new IllegalStateException("CR must be followed by chunk");
                                        }
                                    } else if (messageBag.chunkStatus == ChunkStatus.CHUNK_CR) {
                                        if (oneByte == LF) {
                                            messageBag.chunkStatus = ChunkStatus.CHUNK_CRLF;
                                        } else {
                                            throw new IllegalStateException("LF must be followed by CR");
                                        }
                                    } else if (messageBag.chunkStatus == ChunkStatus.CHUNK_CRLF) {
                                        messageBag.add(oneByte);
                                        messageBag.chunkStatus = ChunkStatus.CHUNK_NUM;
                                    }
                                }
                            }
                        }

                        // 읽기 버퍼를 끝까지 다 읽었습니다.
                        // 지금까지 읽어들인 값을 다시 SelectionKey.attachment에 넣어둡니다.
                        selectionKey.attach(messageBag);

                        if (messageBag.getStatus().equals(Status.TERMINATION)) {
                            // 메시지를 끝까지 다 읽은 것이라면
                            // SelectionKey.attachment 를 제거하고
                            // 지금까지 읽어들인 값을 표시합니다.
                            selectionKey.attach(null);
                            isEnd = true;
                            messageBag.process();
                            break;
                        }
                    }
                }
            }
            socketChannel.close();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                }
            }
            ;
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                }
            }
            ;
        }
    }

}
