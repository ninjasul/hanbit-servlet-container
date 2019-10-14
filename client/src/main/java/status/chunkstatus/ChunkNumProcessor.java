package status.chunkstatus;

import domain.MessageBag;
import status.StatusProcessor;

import static status.chunkstatus.ChunkStatus.CHUNK_NUM_CR;
import static domain.MessageBag.CR;

public class ChunkNumProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        if (curByte == CR) {
            messageBag.setChunkSize(Integer.parseInt(new String(messageBag.toBytes()), 16));
            return CHUNK_NUM_CR.getProcessor();
        }

        messageBag.add(curByte);
        return this;
    }
}
