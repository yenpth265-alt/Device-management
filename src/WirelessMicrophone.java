package devicemanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
public final class WirelessMicrophone extends TeachingDevice {
    public WirelessMicrophone(String id, String name, int year, BigDecimal price) {
        this(id, name, year, price, DeviceStatus.AVAILABLE);
    }
    public WirelessMicrophone(String id, String name, int year, BigDecimal price, DeviceStatus status) {
        super(id, name, year, price, 1, new BigDecimal("20000"), status);
    }
    @Override public BigDecimal calculateAnnualMaintenanceCost() {
        return getPurchasePrice().multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
    }
}
