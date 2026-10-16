package devicemanagement;

public final class Lecturer extends Borrower {
    public Lecturer(String id, String name) { super(id, name); }
    @Override public int getMaximumDevices() { return 3; }
}
