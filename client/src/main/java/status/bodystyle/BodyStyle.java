package status.bodystyle;

import lombok.Getter;
import status.StatusProcessor;

/**
 * HTTP Body의 상태를 세분화합니다.
 */
public enum BodyStyle {
    NO_BODY(new NoBodyProcessor()),
    CONTENT_LENGTH(new ContentLengthProcessor()),
    CHUNKED(new ChunkedProcessor());

    @Getter
    private final StatusProcessor processor;

    BodyStyle(StatusProcessor processor) {
        this.processor = processor;
    }
}