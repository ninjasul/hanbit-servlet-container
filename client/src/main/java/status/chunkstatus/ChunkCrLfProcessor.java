package status.chunkstatus;

import domain.MessageBag;
import status.StatusProcessor;

import static status.chunkstatus.ChunkStatus.CHUNK_NUM;

public class ChunkCrLfProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        messageBag.add(curByte);
        return CHUNK_NUM.getProcessor();
    }
}