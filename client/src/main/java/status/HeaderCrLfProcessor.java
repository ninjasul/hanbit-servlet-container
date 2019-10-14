package status;

import domain.MessageBag;

import static domain.MessageBag.CR;
import static status.Status.HEADER;
import static status.Status.HEADER_CRLFCR;

public class HeaderCrLfProcessor implements StatusProcessor {

    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        if (curByte == CR) {
            return HEADER_CRLFCR.getProcessor();
        }

        messageBag.add(curByte);
        return HEADER.getProcessor();
    }
}