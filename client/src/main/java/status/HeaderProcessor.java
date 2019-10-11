package status;

import domain.MessageBag;

import static domain.HttpClient.CR;
import static status.Status.HEADER_CR;

public class HeaderProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        if (curByte == CR) {
            messageBag.addHeader();
            return HEADER_CR.getProcessor();
        }

        messageBag.add(curByte);
        return this;
    }
}