package status;

import domain.MessageBag;

import static status.Status.REQUEST_LINE;

public class InitProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        return REQUEST_LINE.getProcessor();
    }
}