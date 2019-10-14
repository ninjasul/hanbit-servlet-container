package status.bodystyle;

import domain.MessageBag;
import status.StatusProcessor;

import static status.Status.TERMINATION;

public class ContentLengthProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        // Content-Length 헤더가 있다면 해당 헤더 값만큼 바디를 추가로 읽습니다.
        messageBag.add(curByte);

        if (messageBag.getContentLength() <= messageBag.getBytesSize()) {

            // 메시지 바디를 끝까지 다 읽었다면
            // 지금까지 읽은 값을 bodyBytes에 넣고, 상태를 종료로 표시하고 나갑니다.
            messageBag.setBodyBytes();
            return TERMINATION.getProcessor();
        }

        return this;
    }
}