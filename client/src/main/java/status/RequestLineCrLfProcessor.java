package status;

import domain.MessageBag;

import static status.Status.HEADER;

public class RequestLineCrLfProcessor extends AbstractStatusProcessor {

    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        messageBag.add(curByte);
        return HEADER.getProcessor();
    }
}