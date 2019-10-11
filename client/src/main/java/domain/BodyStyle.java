package domain;

/**
 * HTTP Body의 상태를 세분화합니다.
 */
public enum BodyStyle {
    NO_BODY, CONTENT_LENGTH, CHUNKED
}