package devicemanagement;

public final class TeachingAssistant extends Borrower {
    public TeachingAssistant(String id, String name) { super(id, name); }
    @Override public int getMaximumDevices() { return 2; }
}
