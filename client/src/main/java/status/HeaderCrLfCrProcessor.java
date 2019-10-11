package status;

import domain.BodyStyle;
import domain.MessageBag;

import static domain.HttpClient.LF;
import static status.Status.BODY;
import static status.Status.TERMINATION;

public class HeaderCrLfCrProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {

        // 빈 헤더값을 만나면 이제 헤더부가 끝났고
        // 지금까지 들어온 요청 라인과 헤더를 파싱하여
        // 메시지 바디 유무를 판단하여 더 필요하면 읽기를 계속합니다.
        if (curByte == LF) {
            if (messageBag.afterHeader() == BodyStyle.NO_BODY) {
                return TERMINATION.getProcessor();
            }

            return BODY.getProcessor();
        }

        throw new IllegalStateException("LF must be followed.");
    }
}