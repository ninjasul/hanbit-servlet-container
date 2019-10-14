package status.chunkstatus;

import domain.MessageBag;
import status.StatusProcessor;

import static status.chunkstatus.ChunkStatus.CHUNK_CR;
import static domain.MessageBag.CR;

public class ChunkEndProcessor implements StatusProcessor {

    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        if (curByte == CR) {
            return CHUNK_CR.getProcessor();
        }

        throw new IllegalStateException("CR must be followed by chunk");
    }
}