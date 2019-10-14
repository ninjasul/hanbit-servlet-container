package status;

import lombok.Getter;

/**
 * HTTP 메시지가 가질 수 있는 상태를 표시합니다.
 */
public enum Status {
    INIT(new InitProcessor()),
    REQUEST_LINE(new RequestLineProcessor()),
    REQUEST_LINE_CR(new RequestLineCrProcessor()),
    REQUEST_LINE_CRLF(new RequestLineCrLfProcessor()),
    HEADER(new HeaderProcessor()),
    HEADER_CR(new HeaderCrProcessor()),
    HEADER_CRLF(new HeaderCrLfProcessor()),
    HEADER_CRLFCR(new HeaderCrLfCrProcessor()),
    BODY(new BodyProcessor()),
    TERMINATION(new TerminationProcessor());

    @Getter
    private final StatusProcessor processor;

    Status(StatusProcessor processor) {
        this.processor = processor;
    }
}