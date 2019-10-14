package status.chunkstatus;

import domain.MessageBag;
import status.StatusProcessor;

import static status.chunkstatus.ChunkStatus.CHUNK_CRLF;
import static domain.MessageBag.LF;

public class ChunkCrProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        if (curByte == LF) {
            return CHUNK_CRLF.getProcessor();
        }

        throw new IllegalStateException("LF must be followed by CR");
    }
}