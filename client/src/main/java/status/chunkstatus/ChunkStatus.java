package status.chunkstatus;

import lombok.Getter;
import status.StatusProcessor;

/**
 * HTTP Body 중에서 청크 타입일 때의 상태를 표시합니다.
 * 청크가 숫자, CR, LF, 앞선 숫자만큼의 바이트 배열로 구성됩니다.
 */
public enum ChunkStatus {
    CHUNK_NUM(new ChunkNumProcessor()),
    CHUNK_NUM_CR(new ChunkNumCrProcessor()),
    CHUNK_NUM_CRLF(new ChunkNumCrLfProcessor()),
    CHUNK_BODY(new ChunkBodyProcessor()),
    CHUNK_END(new ChunkEndProcessor()),
    CHUNK_CR(new ChunkCrProcessor()),
    CHUNK_CRLF(new ChunkCrLfProcessor());

    @Getter
    private final StatusProcessor processor;

    ChunkStatus(StatusProcessor processor) {
        this.processor = processor;
    }
}