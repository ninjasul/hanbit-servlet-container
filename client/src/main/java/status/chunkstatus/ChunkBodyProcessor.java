package status.chunkstatus;

import domain.MessageBag;
import status.StatusProcessor;

import static status.chunkstatus.ChunkStatus.CHUNK_END;

public class ChunkBodyProcessor implements StatusProcessor {

    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        if (messageBag.getBytesSize() == messageBag.getChunkSize() - 1) {
            messageBag.add(curByte);
            messageBag.addChunk();

            return CHUNK_END.getProcessor();
        }

        messageBag.add(curByte);
        return this;
    }
}