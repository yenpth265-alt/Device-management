package devicemanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
public final class BorrowingSlip {
    private final String slipId;
    private final IBorrower borrower;
    private final String teachingRoom;
    private final LocalDate borrowedDate;
    private final List<BorrowItem> items = new ArrayList<>();
    public BorrowingSlip(String id, IBorrower borrower, String room, LocalDate date) {
        slipId = Validation.text(id, "Mã phiếu");
        this.borrower = Objects.requireNonNull(borrower, "Người mượn không được null.");
        Validation.text(borrower.getBorrowerId(), "Mã người mượn");
        Validation.text(borrower.getFullName(), "Họ tên");
        if (borrower.getMaximumDevices() <= 0) throw new IllegalArgumentException("Giới hạn mượn phải dương.");
        teachingRoom = Validation.text(room, "Phòng dạy");
        borrowedDate = Objects.requireNonNull(date, "Ngày mượn không được null.");
    }
    public String getSlipId() { return slipId; }
    public IBorrower getBorrower() { return borrower; }
    public String getTeachingRoom() { return teachingRoom; }
    public LocalDate getBorrowedDate() { return borrowedDate; }
    public List<BorrowItem> getItems() { return Collections.unmodifiableList(items); }
    public void addDevice(TeachingDevice device, int borrowDays) {
        Objects.requireNonNull(device, "Thiết bị không được null.");
        if (borrowDays <= 0 || borrowDays > device.getMaximumBorrowDays())
            throw new InvalidBorrowPeriodException(device.getDeviceId(), borrowDays, device.getMaximumBorrowDays());
        if (items.stream().anyMatch(i -> i.getDevice().getDeviceId().equalsIgnoreCase(device.getDeviceId())))
            throw new DuplicateDeviceException(device.getDeviceId());
        if (items.size() >= borrower.getMaximumDevices())
            throw new BorrowLimitExceededException(borrower.getBorrowerId(), borrower.getMaximumDevices());
        if (device.getStatus() != DeviceStatus.AVAILABLE)
            throw new DeviceUnavailableException(device.getDeviceId(), device.getStatus());
        // Tạo item và thêm vào list trước phép gán trạng thái không ném ngoại lệ.
        // Nếu tính hạn trả/cấp phát thất bại, trạng thái thiết bị chưa bị đổi.
        BorrowItem item = new BorrowItem(device, borrowedDate, borrowedDate.plusDays(borrowDays));
        items.add(item);
        device.commitBorrow();
    }
    public BigDecimal returnDevice(String id, LocalDate date) {
        String key = Validation.text(id, "Mã thiết bị");
        BorrowItem item = items.stream().filter(i -> i.getDevice().getDeviceId().equalsIgnoreCase(key))
            .findFirst().orElseThrow(() -> new DeviceNotInSlipException(key));
        // Mọi điều kiện nghiệp vụ được kiểm tra trước khi đổi dữ liệu.
        if (item.isReturned()) throw new DeviceAlreadyReturnedException(key);
        Objects.requireNonNull(date, "Ngày trả không được null.");
        if (date.isBefore(borrowedDate)) throw new InvalidReturnDateException(borrowedDate, date);
        BigDecimal fee = item.calculateLateFee(date);
        item.returnDevice(date);
        return fee;
    }
    public BigDecimal calculateTotalLateFee() {
        return items.stream().filter(BorrowItem::isReturned)
            .map(i -> i.calculateLateFee(i.getReturnedDate())).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal calculateEstimatedLateFee(LocalDate asOf) {
        Objects.requireNonNull(asOf, "Ngày tính phí không được null.");
        return items.stream().map(i -> i.calculateLateFee(asOf)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public boolean hasUnreturnedItems() { return items.stream().anyMatch(i -> !i.isReturned()); }
    public boolean hasLateReturns() {
        return items.stream().anyMatch(i -> i.isReturned() && i.getReturnedDate().isAfter(i.getDueDate()));
    }
    public boolean isOverdue(LocalDate asOf) {
        Objects.requireNonNull(asOf);
        return items.stream().anyMatch(i -> !i.isReturned() && asOf.isAfter(i.getDueDate()));
    }
    @Override public String toString() {
        return slipId + " | " + borrower.getFullName() + " | " + teachingRoom + " | " + borrowedDate + " | " + items.size() + " thiết bị";
    }
}
