package domain;

/**
 * HTTP Body 중에서 청크 타입일 때의 상태를 표시합니다.
 * 청크가 숫자, CR, LF, 앞선 숫자만큼의 바이트 배열로 구성됩니다.
 */
public enum ChunkStatus {
    CHUNK_NUM, CHUNK_NUM_CR, CHUNK_NUM_CRLF,
    CHUNK_BODY,
    CHUNK_END, CHUNK_CR, CHUNK_CRLF
}