package status.bodystyle;

import domain.MessageBag;
import status.StatusProcessor;

public class ChunkedProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        return messageBag.getChunkedProcessor().proceed(messageBag, curByte);
    }
}