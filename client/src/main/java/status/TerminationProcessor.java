package status;

public class TerminationProcessor extends AbstractStatusProcessor {
    @Override
    public boolean isTerminated() {
        return true;
    }
}