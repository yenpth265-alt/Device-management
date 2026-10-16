package devicemanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
public final class BorrowItem {
    private final TeachingDevice device;
    private final LocalDate borrowedDate;
    private final LocalDate dueDate;
    private LocalDate returnedDate;
    // Chỉ phiếu mượn tạo item; tránh item độc lập chiếm/trả nhầm thiết bị.
    BorrowItem(TeachingDevice device, LocalDate borrowedDate, LocalDate dueDate) {
        this.device = Objects.requireNonNull(device, "Thiết bị không được null.");
        this.borrowedDate = Objects.requireNonNull(borrowedDate, "Ngày mượn không được null.");
        this.dueDate = Objects.requireNonNull(dueDate, "Hạn trả không được null.");
        if (dueDate.isBefore(borrowedDate)) throw new IllegalArgumentException("Hạn trả trước ngày mượn.");
    }
    public TeachingDevice getDevice() { return device; }
    public LocalDate getBorrowedDate() { return borrowedDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnedDate() { return returnedDate; }
    public boolean isReturned() { return returnedDate != null; }
    public BigDecimal calculateLateFee(LocalDate asOf) {
        Objects.requireNonNull(asOf, "Ngày tính phí không được null.");
        LocalDate end = isReturned() ? returnedDate : asOf;
        long days = Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
        return device.getDailyLateFee().multiply(BigDecimal.valueOf(days));
    }
    // 'return' là từ khóa Java, nên phương thức được đặt tên returnDevice.
    public void returnDevice(LocalDate date) {
        if (isReturned()) throw new DeviceAlreadyReturnedException(device.getDeviceId());
        Objects.requireNonNull(date, "Ngày trả không được null.");
        if (date.isBefore(borrowedDate)) throw new InvalidReturnDateException(borrowedDate, date);
        returnedDate = date;
        device.commitReturn();
    }
    @Override public String toString() {
        return device.getDeviceId() + " | mượn: " + borrowedDate + " | hạn: " + dueDate
            + " | trả: " + (returnedDate == null ? "Chưa trả" : returnedDate);
    }
}
