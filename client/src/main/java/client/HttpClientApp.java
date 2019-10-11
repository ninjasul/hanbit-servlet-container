package client;

import domain.HttpClient;

import java.util.HashMap;
import java.util.Map;

public class HttpClientApp {

    public static void main(String[] args) {

        // HTTP 요청을 전달할 호스트명과 IP를 설정합니다.
        String host = "endofhope.com";
        int port = 80;

        // HTTP 요청의 시작 줄을 설정합니다.
        // GET 방식으로 /를 HTTP 1.1 버전을 사용해 요청합니다.
        String requestLine = "GET / HTTP/1.1";

        // HTTP 헤더를 설정합니다.
        // 먼저 Host 헤더에 앞서 입력받은 호스트명:포트를 지정합니다.
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Host", host + ":" + port);

        // 이번 예에서는 GET 방식 호출이므로 HTTP 바디는 없습니다.
        byte[] bodyBytes = null;

        // 지금까지 생성한 정보를 생성자에 추가해 객체를 생성하고 send 메서드를 호출합니다.
        HttpClient hc = new HttpClient(host, port, requestLine, headerMap, bodyBytes);
        hc.send();
    }
}