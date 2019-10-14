package status;

import domain.MessageBag;

public abstract class AbstractStatusProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        messageBag.add(curByte);
        return this;
    }
}