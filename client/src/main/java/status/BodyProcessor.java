package status;

import domain.MessageBag;

public class BodyProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        return messageBag.getBodyStyleProcessor().proceed(messageBag, curByte);
    }
}