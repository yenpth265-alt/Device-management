package devicemanagement;
import java.time.LocalDate;
public class BorrowingException extends RuntimeException {
    public BorrowingException(String message) { super(message); }
    public BorrowingException(String message, Throwable cause) { super(message, cause); }
}
class BorrowLimitExceededException extends BorrowingException {
    private final String borrowerId;
    private final int maximumDevices;
    public BorrowLimitExceededException(String id, int maximum) {
        super("Người mượn " + id + " chỉ được mượn tối đa " + maximum + " thiết bị/phiếu.");
        borrowerId = id; maximumDevices = maximum;
    }
    public String getBorrowerId() { return borrowerId; }
    public int getMaximumDevices() { return maximumDevices; }
}
class DeviceAlreadyReturnedException extends BorrowingException {
    private final String deviceId;
    public DeviceAlreadyReturnedException(String id) { super("Thiết bị " + id + " đã được trả trước đó."); deviceId = id; }
    public String getDeviceId() { return deviceId; }
}
class DeviceNotInSlipException extends BorrowingException {
    private final String deviceId;
    public DeviceNotInSlipException(String id) { super("Thiết bị " + id + " không thuộc phiếu."); deviceId = id; }
    public String getDeviceId() { return deviceId; }
}
class DeviceUnavailableException extends BorrowingException {
    private final String deviceId;
    private final DeviceStatus currentStatus;
    public DeviceUnavailableException(String id, DeviceStatus status) {
        super("Thiết bị " + id + " không sẵn sàng: " + status);
        deviceId = id; currentStatus = status;
    }
    public String getDeviceId() { return deviceId; }
    public DeviceStatus getCurrentStatus() { return currentStatus; }
}
class DuplicateDeviceException extends BorrowingException {
    private final String deviceId;
    public DuplicateDeviceException(String id) { super("Thiết bị " + id + " đã có trong phiếu."); deviceId = id; }
    public String getDeviceId() { return deviceId; }
}
class InvalidBorrowPeriodException extends BorrowingException {
    private final String deviceId;
    private final int requestedDays;
    private final int maximumDays;
    public InvalidBorrowPeriodException(String id, int requested, int maximum) {
        super("Thiết bị " + id + ": yêu cầu " + requested + " ngày; chỉ cho phép 1 đến " + maximum + " ngày.");
        deviceId = id; requestedDays = requested; maximumDays = maximum;
    }
    public String getDeviceId() { return deviceId; }
    public int getRequestedDays() { return requestedDays; }
    public int getMaximumDays() { return maximumDays; }
}
class InvalidReturnDateException extends BorrowingException {
    public InvalidReturnDateException(LocalDate borrowed, LocalDate returned) {
        super("Ngày trả " + returned + " trước ngày mượn " + borrowed + ".");
    }
}

