package status;

import domain.MessageBag;

import static status.Status.REQUEST_LINE_CRLF;

public class RequestLineCrProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        return REQUEST_LINE_CRLF.getProcessor();
    }
}