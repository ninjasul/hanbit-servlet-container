package status.chunkstatus;

import domain.MessageBag;
import status.StatusProcessor;

import static status.chunkstatus.ChunkStatus.CHUNK_BODY;
import static domain.MessageBag.LF;
import static status.Status.TERMINATION;

public class ChunkNumCrProcessor implements StatusProcessor {
    @Override
    public StatusProcessor proceed(MessageBag messageBag, byte curByte) {
        if (curByte == LF) {
            if (messageBag.getChunkSize() == 0) {
                // 크기가 0인 청크는 청크 방식 메시지 바디의 끝을 의미합니다.
                // 메시지 상태를 종료로 표시하고 나갑니다.
                messageBag.setChunkBodyBytes();
                return TERMINATION.getProcessor();
            }

            return CHUNK_BODY.getProcessor();
        }

        throw new IllegalStateException("LF must be followed by CR");
    }
}