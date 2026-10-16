package devicemanagement;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
public final class ClassRoom {
    private final String roomId;
    private final String name;
    private final int capacity;
    private final List<Device> devices = new ArrayList<>();
    public ClassRoom(String id, String name, int capacity) {
        roomId = Validation.text(id, "Mã phòng");
        this.name = Validation.text(name, "Tên phòng");
        if (capacity <= 0) throw new IllegalArgumentException("Sức chứa phải dương.");
        this.capacity = capacity;
    }
    public String getRoomId() { return roomId; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public List<Device> getDevices() { return Collections.unmodifiableList(devices); }
    public void addDevice(Device device) {
        Objects.requireNonNull(device, "Thiết bị không được null.");
        if (findDevice(device.getDeviceId()) != null) throw new IllegalArgumentException("Trùng mã thiết bị trong phòng.");
        devices.add(device);
    }
    public boolean removeDevice(String id) {
        Device device = findDevice(id);
        return device != null && devices.remove(device);
    }
    public Device findDevice(String id) {
        String key = Validation.text(id, "Mã thiết bị");
        return devices.stream().filter(d -> d.getDeviceId().equalsIgnoreCase(key)).findFirst().orElse(null);
    }
    public BigDecimal calculateAnnualMaintenanceCost() {
        // Đa hình: không kiểm tra loại thiết bị bằng instanceof hoặc switch.
        return devices.stream().map(Device::calculateAnnualMaintenanceCost).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public List<Device> getDevicesRequiringMaintenance() {
        List<Device> result = new ArrayList<>();
        for (Device d : devices)
            if (d.getStatus() == DeviceStatus.UNDER_MAINTENANCE || Year.now().getValue() - d.getYearOfUse() > 5)
                result.add(d);
        return result;
    }
}
