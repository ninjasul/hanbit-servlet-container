package status.bodystyle;

import status.StatusProcessor;

public class NoBodyProcessor implements StatusProcessor {
    @Override
    public boolean isTerminated() {
        return true;
    }
}