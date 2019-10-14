package status;

import domain.MessageBag;

public interface StatusProcessor {
    default StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        messageBag.add(curByte);
        return this;
    }

    default boolean isTerminated() {
        return false;
    }
}