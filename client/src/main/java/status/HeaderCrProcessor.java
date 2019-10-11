package status;

import domain.MessageBag;

import static domain.HttpClient.LF;
import static status.Status.HEADER_CRLF;

public class HeaderCrProcessor implements StatusProcessor {

    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        if (curByte == LF) {
            return HEADER_CRLF.getProcessor();
        }

        throw new IllegalStateException("LF must be followed.");
    }
}