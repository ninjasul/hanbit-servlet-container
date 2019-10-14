package domain;

import lombok.Getter;
import lombok.Setter;
import status.InitProcessor;
import status.StatusProcessor;

import java.util.*;

import static status.Status.TERMINATION;
import static status.bodystyle.BodyStyle.CHUNKED;
import static status.bodystyle.BodyStyle.CONTENT_LENGTH;


public class MessageBag {
    public static final byte CR = '\r';
    public static final byte LF = '\n';

    private StatusProcessor statusProcessor = new InitProcessor();

    private List<Byte> byteList = new ArrayList<Byte>();
    private String requestLine;
    private Map<String, String> headerMap = new HashMap<String, String>();

    @Getter @Setter
    private int chunkSize = -1;
    private byte[] bodyBytes;

    // 청크 방식 바디의 경우 여러 청크들이 하나의 바디를 이루므로
    // 리스트 형태로 유지합니다.
    private List<byte[]> chunkList = new ArrayList<>();

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
        return Optional.ofNullable(headerMap.getOrDefault("Content-Length", "0"))
                .map(Integer::parseInt)
                .orElse(0);
    }

    // 헤더가 다 들어온 것이 확인되면
    // 각각의 이름/값 쌍으로 헤더를 재 구성하고
    // 메시지 바디가 있는지,얼마나
    // 혹은 어떻게 메시지 바디를 읽어야 하는지 확인하는 역할을 하는 메서드입니다.
    public StatusProcessor getBodyStyleProcessor() {
        statusProcessor = TERMINATION.getProcessor();

        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            if ("Content-Length".equals(entry.getKey())) {
                statusProcessor = CONTENT_LENGTH.getProcessor();
            }
            else if ("Transfer-Encoding".equals(entry.getKey()) && "chunked".equals(entry.getValue())) {
                statusProcessor = CHUNKED.getProcessor();
            }
        }

        return statusProcessor;
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

    public boolean isTerminated() {
        return statusProcessor.isTerminated();
    }

    public void proceed(byte curByte) {
        statusProcessor = statusProcessor.proceed(this, curByte);
    }
}