package status.bodystyle;

import lombok.Getter;
import status.StatusProcessor;
import status.chunkstatus.ChunkNumProcessor;

/**
 * HTTP Body의 상태를 세분화합니다.
 */
public enum BodyStyle {
    CONTENT_LENGTH(new ContentLengthProcessor()),
    CHUNKED(new ChunkNumProcessor());

    @Getter
    private final StatusProcessor processor;

    BodyStyle(StatusProcessor processor) {
        this.processor = processor;
    }
}