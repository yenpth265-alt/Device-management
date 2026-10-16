package devicemanagement;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Objects;

public abstract class Device {
    private final String deviceId;
    private final String name;
    private final int yearOfUse;
    private final BigDecimal purchasePrice;
    private DeviceStatus status;

    protected Device(String id, String name, int year, BigDecimal price, DeviceStatus status) {
        this.deviceId = Validation.text(id, "Mã thiết bị");
        this.name = Validation.text(name, "Tên thiết bị");
        if (year < 1 || year > Year.now().getValue()) throw new IllegalArgumentException("Năm sử dụng không hợp lệ.");
        Objects.requireNonNull(price, "Giá mua không được null.");
        if (price.signum() <= 0) throw new IllegalArgumentException("Giá mua phải lớn hơn 0.");
        Objects.requireNonNull(status, "Trạng thái không được null.");
        if (status == DeviceStatus.BORROWED) throw new IllegalArgumentException("Thiết bị mới không thể đang được mượn.");
        this.yearOfUse = year; this.purchasePrice = price; this.status = status;
    }
    public String getDeviceId() { return deviceId; }
    public String getName() { return name; }
    public int getYearOfUse() { return yearOfUse; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public DeviceStatus getStatus() { return status; }
    public abstract BigDecimal calculateAnnualMaintenanceCost();
    public final void startMaintenance() {
        if (status != DeviceStatus.AVAILABLE) throw new DeviceUnavailableException(deviceId, status);
        status = DeviceStatus.UNDER_MAINTENANCE;
    }
    public final void finishMaintenance() {
        if (status != DeviceStatus.UNDER_MAINTENANCE) throw new IllegalStateException("Thiết bị không đang bảo trì.");
        status = DeviceStatus.AVAILABLE;
    }
    // Package-private: giao diện không được tự đổi trạng thái mượn/trả.
    final void commitBorrow() { status = DeviceStatus.BORROWED; }
    final void commitReturn() { status = DeviceStatus.AVAILABLE; }
    @Override public String toString() {
        return deviceId + " | " + name + " | " + yearOfUse + " | " + purchasePrice.toPlainString() + " VND | " + status;
    }
}
