package status;

import domain.MessageBag;

public interface StatusProcessor {
    //void setNext(MessageBag messageBag);
    default StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        messageBag.add(curByte);
        return this;
    }
}