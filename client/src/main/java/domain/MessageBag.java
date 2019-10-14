package domain;

import status.bodystyle.BodyStyle;
import status.chunkstatus.ChunkStatus;
import lombok.Getter;
import lombok.Setter;
import status.InitProcessor;
import status.StatusProcessor;

import java.util.*;


public class MessageBag {
    public static final byte CR = '\r';
    public static final byte LF = '\n';

    // 이 클래스는 HTTP 메시지를 구성하는 구성 요소를 표현할 수 있게 구성되었습니다.
    //@Getter @Setter
    //private Status status = Status.INIT;

    @Getter
    private StatusProcessor statusProcessor = new InitProcessor();

    private StatusProcessor bodyStyleProcessor;

    private StatusProcessor chunkedProcessor;

    private List<Byte> byteList = new ArrayList<Byte>();
    private String requestLine;
    private Map<String, String> headerMap = new HashMap<String, String>();
    private int contentLength;

    private BodyStyle bodyStyle;

    private ChunkStatus chunkStatus;

    @Getter @Setter
    private int chunkSize = -1;
    private byte[] bodyBytes;

    // 청크 방식 바디의 경우 여러 청크들이 하나의 바디를 이루므로
    // 리스트 형태로 유지합니다.
    private List<byte[]> chunkList = new ArrayList<byte[]>();

    public byte[] toBytes() {
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            bytes[i] = byteList.get(i);
        }
        byteList.clear();
        return bytes;
    }

    public void add(byte oneByte) {
        byteList.add(oneByte);
    }

    public int getBytesSize() {
        return byteList.size();
    }

    public String getRequestLine() {
        return requestLine;
    }

    public void setRequestLine() {
        requestLine = new String(toBytes());
    }

    public void addHeader() {
        String headerLine = new String(toBytes());
        int indexOfColon = headerLine.indexOf(":");
        headerMap.put(headerLine.substring(0, indexOfColon).trim(),
                headerLine.substring(indexOfColon + 1).trim());
    }

    public int getContentLength() {
        return contentLength;
    }

    // 헤더가 다 들어온 것이 확인되면
    // 각각의 이름/값 쌍으로 헤더를 재 구성하고
    // 메시지 바디가 있는지,얼마나
    // 혹은 어떻게 메시지 바디를 읽어야 하는지 확인하는 역할을 하는 메서드입니다.
    public BodyStyle afterHeader() {
        bodyStyle = BodyStyle.NO_BODY;
        Set<String> headerKeySet = headerMap.keySet();
        Iterator<String> headerKeyIter = headerKeySet.iterator();
        while (headerKeyIter.hasNext()) {
            String headerName = headerKeyIter.next();
            String headerValue = headerMap.get(headerName);
            if ("Content-Length".equals(headerName)) {
                contentLength = Integer.parseInt(headerValue);
                bodyStyle = BodyStyle.CONTENT_LENGTH;
            } else if ("Transfer-Encoding".equals(headerName) &&
                    "chunked".equals(headerValue)) {
                bodyStyle = BodyStyle.CHUNKED;
                chunkStatus = ChunkStatus.CHUNK_NUM;
            }
        }
        return bodyStyle;
    }

    public void setBodyBytes() {
        bodyBytes = toBytes();
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public void addChunk() {
        chunkList.add(toBytes());
    }

    // 여러 청크 바디를 하나의 바디로 묶어주는 유틸리티 메서드입니다.
    public void setChunkBodyBytes() {
        int bodyBytesLength = 0;
        for (int i = 0; i < chunkList.size(); i++) {
            bodyBytesLength = +chunkList.get(i).length;
        }
        bodyBytes = new byte[bodyBytesLength];
        int destPos = 0;
        for (int i = 0; i < chunkList.size(); i++) {
            System.arraycopy(chunkList.get(i), 0, bodyBytes, destPos,
                    chunkList.get(i).length);
            destPos = +chunkList.get(i).length;
        }
    }

    // 읽어들인 HTTP 메시지를 보여주기 위한 메서드입니다.
    public void process() {
        System.out.printf("%s\n", requestLine);
        Set<String> headerKeySet = headerMap.keySet();
        Iterator<String> headerKeyIter = headerKeySet.iterator();
        while (headerKeyIter.hasNext()) {
            String headerName = headerKeyIter.next();
            String headerValue = headerMap.get(headerName);
            System.out.printf("%s: %s\n", headerName, headerValue);
        }
        System.out.printf("\n");
        if (bodyBytes != null) {
            System.out.println(new String(getBodyBytes()));
        }
    }

    public void proceed(byte curByte) {
        statusProcessor = statusProcessor.proceed(this, curByte);
    }


    public StatusProcessor getBodyStyleProcessor() {
        return bodyStyle.getProcessor();
    }

    public StatusProcessor getChunkedProcessor() {
        return chunkStatus.getProcessor();
    }
}