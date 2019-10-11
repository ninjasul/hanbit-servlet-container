package status;

import domain.MessageBag;

import static domain.HttpClient.CR;
import static status.Status.REQUEST_LINE_CR;

public class RequestLineProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        if (curByte == CR) {
            messageBag.setRequestLine();
            return REQUEST_LINE_CR.getProcessor();
        }

        messageBag.add(curByte);
        return this;
    }
}