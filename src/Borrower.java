package devicemanagement;

public abstract class Borrower implements IBorrower {
    private final String borrowerId;
    private final String fullName;
    protected Borrower(String id, String name) {
        borrowerId = Validation.text(id, "Mã người mượn");
        fullName = Validation.text(name, "Họ tên");
    }
    @Override public String getBorrowerId() { return borrowerId; }
    @Override public String getFullName() { return fullName; }
    @Override public abstract int getMaximumDevices();
    @Override public String toString() {
        return borrowerId + " | " + fullName + " | tối đa " + getMaximumDevices() + " thiết bị/phiếu";
    }
}
