package devicemanagement;

import java.math.BigDecimal;
import java.util.Objects;
public abstract class TeachingDevice extends Device {
    private final int maximumBorrowDays;
    private final BigDecimal dailyLateFee;
    protected TeachingDevice(String id, String name, int year, BigDecimal price,
            int maxDays, BigDecimal lateFee, DeviceStatus status) {
        super(id, name, year, price, status);
        if (maxDays <= 0) throw new IllegalArgumentException("Giới hạn ngày mượn phải dương.");
        Objects.requireNonNull(lateFee, "Phí không được null.");
        if (lateFee.signum() < 0) throw new IllegalArgumentException("Phí không được âm.");
        maximumBorrowDays = maxDays; dailyLateFee = lateFee;
    }
    public int getMaximumBorrowDays() { return maximumBorrowDays; }
    public BigDecimal getDailyLateFee() { return dailyLateFee; }
}
