package devicemanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
public final class ProjectorRemote extends TeachingDevice {
    public ProjectorRemote(String id, String name, int year, BigDecimal price) {
        this(id, name, year, price, DeviceStatus.AVAILABLE);
    }
    public ProjectorRemote(String id, String name, int year, BigDecimal price, DeviceStatus status) {
        super(id, name, year, price, 2, new BigDecimal("10000"), status);
    }
    @Override public BigDecimal calculateAnnualMaintenanceCost() {
        return getPurchasePrice().multiply(new BigDecimal("0.07")).setScale(2, RoundingMode.HALF_UP);
    }
}
