package status;

import domain.MessageBag;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractStatusProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        messageBag.add(curByte);
        return this;
    }
}