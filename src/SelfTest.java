package devicemanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

/** Chạy độc lập, không cần JUnit; mỗi ca tạo dữ liệu riêng. */
public final class SelfTest {
    private static int passed;
    private static int failed;
    private static final LocalDate DAY = LocalDate.of(2026, 1, 10);
    private static WirelessMicrophone mic(String id) { return new WirelessMicrophone(id, "Micro", 2020, new BigDecimal("1000000")); }
    private static ProjectorRemote remote(String id) { return new ProjectorRemote(id, "Điều khiển", 2020, new BigDecimal("500000")); }
    private static BorrowingSlip slip(IBorrower b) { return new BorrowingSlip("P1", b, "D9", DAY); }
    private static BorrowingSlip slip() { return slip(new Lecturer("GV", "Giảng viên")); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void money(BigDecimal actual, String expected) { check(actual.compareTo(new BigDecimal(expected)) == 0, "Sai tiền: " + actual); }
    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); } catch (Throwable ex) {
            if (type.isInstance(ex)) return;
            throw new AssertionError("Cần " + type.getSimpleName() + ", nhận " + ex, ex);
        }
        throw new AssertionError("Không phát sinh " + type.getSimpleName());
    }
    private static void test(String name, Runnable action) {
        try { action.run(); passed++; System.out.println("PASS | " + name); }
        catch (Throwable ex) { failed++; System.out.println("FAIL | " + name + " | " + ex); }
    }
    public static void main(String[] args) {
        passed = 0; failed = 0;
        test("Mượn và trả đúng hạn", () -> {
            BorrowingSlip s = slip(); TeachingDevice d = remote("D1"); s.addDevice(d, 2);
            check(s.getItems().size() == 1 && d.getStatus() == DeviceStatus.BORROWED, "Sai trạng thái mượn");
            money(s.returnDevice("d1", DAY.plusDays(2)), "0");
            check(d.getStatus() == DeviceStatus.AVAILABLE && !s.hasUnreturnedItems(), "Sai trạng thái trả");
        });
        test("Micro trả muộn 2 ngày", () -> {
            BorrowingSlip s = slip(); s.addDevice(mic("M1"), 1);
            money(s.returnDevice("M1", DAY.plusDays(3)), "40000"); money(s.calculateTotalLateFee(), "40000");
            check(s.hasLateReturns(), "Thiếu phiếu trả muộn");
        });
        test("Mượn thiết bị bảo trì không đổi dữ liệu", () -> {
            BorrowingSlip s = slip(); TeachingDevice d = remote("D1"); d.startMaintenance();
            expect(DeviceUnavailableException.class, () -> s.addDevice(d, 1));
            check(s.getItems().isEmpty() && d.getStatus() == DeviceStatus.UNDER_MAINTENANCE, "Dữ liệu bị đổi");
        });
        test("Trợ giảng mượn thiết bị thứ ba", () -> {
            BorrowingSlip s = slip(new TeachingAssistant("TG", "Trợ giảng"));
            s.addDevice(remote("D1"), 1); s.addDevice(remote("D2"), 1); TeachingDevice third = remote("D3");
            expect(BorrowLimitExceededException.class, () -> s.addDevice(third, 1));
            check(s.getItems().size() == 2 && third.getStatus() == DeviceStatus.AVAILABLE, "Dữ liệu bị đổi");
        });
        test("Thêm cùng micro hai lần", () -> {
            BorrowingSlip s = slip(); TeachingDevice d = mic("M1"); s.addDevice(d, 1);
            expect(DuplicateDeviceException.class, () -> s.addDevice(d, 1));
            check(s.getItems().size() == 1 && d.getStatus() == DeviceStatus.BORROWED, "Dữ liệu bị đổi");
        });
        test("Micro 3 ngày vượt giới hạn 1 ngày", () -> {
            BorrowingSlip s = slip(); TeachingDevice d = mic("M1");
            expect(InvalidBorrowPeriodException.class, () -> s.addDevice(d, 3));
            check(s.getItems().isEmpty() && d.getStatus() == DeviceStatus.AVAILABLE, "Dữ liệu bị đổi");
        });
        test("Trả thiết bị ngoài phiếu", () -> {
            BorrowingSlip s = slip(); TeachingDevice d = mic("M1"); s.addDevice(d, 1);
            expect(DeviceNotInSlipException.class, () -> s.returnDevice("UNKNOWN", DAY));
            check(!s.getItems().get(0).isReturned() && d.getStatus() == DeviceStatus.BORROWED, "Dữ liệu bị đổi");
        });
        test("Trả hai lần giữ nguyên ngày trả", () -> {
            BorrowingSlip s = slip(); TeachingDevice d = mic("M1"); s.addDevice(d, 1); s.returnDevice("M1", DAY.plusDays(1));
            expect(DeviceAlreadyReturnedException.class, () -> s.returnDevice("M1", DAY.plusDays(3)));
            check(s.getItems().get(0).getReturnedDate().equals(DAY.plusDays(1)) && d.getStatus() == DeviceStatus.AVAILABLE, "Dữ liệu bị đổi");
        });
        test("Ngày trả trước ngày mượn", () -> {
            BorrowingSlip s = slip(); TeachingDevice d = mic("M1"); s.addDevice(d, 1);
            expect(InvalidReturnDateException.class, () -> s.returnDevice("M1", DAY.minusDays(1)));
            check(!s.getItems().get(0).isReturned() && d.getStatus() == DeviceStatus.BORROWED, "Dữ liệu bị đổi");
        });
        test("Số ngày 0/âm và thiết bị null", () -> {
            BorrowingSlip s = slip(); TeachingDevice d = mic("M1");
            expect(InvalidBorrowPeriodException.class, () -> s.addDevice(d, 0));
            expect(InvalidBorrowPeriodException.class, () -> s.addDevice(d, -1));
            expect(NullPointerException.class, () -> s.addDevice(null, 1));
            check(s.getItems().isEmpty() && d.getStatus() == DeviceStatus.AVAILABLE, "Dữ liệu bị đổi");
        });
        test("Thiết bị đang được mượn ở phiếu khác", () -> {
            TeachingDevice d = mic("M1"); BorrowingSlip a = slip(), b = slip(); a.addDevice(d, 1);
            expect(DeviceUnavailableException.class, () -> b.addDevice(d, 1));
            check(b.getItems().isEmpty() && a.getItems().size() == 1, "Dữ liệu bị đổi");
        });
        test("Trả rồi mượn lại ở phiếu mới; không trả lại qua phiếu cũ", () -> {
            TeachingDevice d = mic("M1"); BorrowingSlip a = slip(), b = slip(); a.addDevice(d, 1); a.returnDevice("M1", DAY);
            b.addDevice(d, 1); expect(DeviceAlreadyReturnedException.class, () -> a.returnDevice("M1", DAY));
            check(d.getStatus() == DeviceStatus.BORROWED, "Phiếu cũ giải phóng thiết bị đang mượn");
        });
        test("Giới hạn tính cả item đã trả", () -> {
            BorrowingSlip s = slip(new TeachingAssistant("TG", "Trợ giảng"));
            s.addDevice(remote("D1"), 1); s.addDevice(remote("D2"), 1); s.returnDevice("D1", DAY);
            expect(BorrowLimitExceededException.class, () -> s.addDevice(remote("D3"), 1));
        });
        test("Danh sách item chỉ đọc", () -> {
            BorrowingSlip s = slip(); s.addDevice(mic("M1"), 1);
            expect(UnsupportedOperationException.class, () -> s.getItems().clear());
            check(s.getItems().size() == 1, "Mất item");
        });
        test("Quá hạn và phí tạm tính tách khỏi phí đã chốt", () -> {
            BorrowingSlip s = slip(); s.addDevice(mic("M1"), 1);
            check(!s.isOverdue(DAY.plusDays(1)) && s.isOverdue(DAY.plusDays(3)), "Sai biên hạn trả");
            check(s.hasUnreturnedItems() && !s.hasLateReturns(), "Sai loại phiếu");
            money(s.calculateTotalLateFee(), "0"); money(s.calculateEstimatedLateFee(DAY.plusDays(3)), "40000");
        });
        test("Phòng: null, trùng mã, tìm/xóa và kết tập", () -> {
            ClassRoom r = new ClassRoom("R1", "Phòng", 40); TeachingDevice d = mic("M1"); r.addDevice(d);
            expect(NullPointerException.class, () -> r.addDevice(null));
            expect(IllegalArgumentException.class, () -> r.addDevice(mic("m1")));
            check(r.findDevice("m1") == d && r.removeDevice("M1") && !r.removeDevice("M1"), "Tìm/xóa sai");
            check(d.getStatus() == DeviceStatus.AVAILABLE, "Xóa phòng làm hỏng thiết bị");
        });
        test("Bảo trì đa hình", () -> {
            ClassRoom r = new ClassRoom("R1", "Phòng", 40);
            r.addDevice(mic("M1")); r.addDevice(remote("D1"));
            r.addDevice(new AirConditionerRemote("A1", "Điều hòa", 2020, new BigDecimal("300000")));
            money(r.calculateAnnualMaintenanceCost(), "150000");
        });
        test("Bảo trì: đúng 5 năm, trên 5 năm, trạng thái bảo trì", () -> {
            int y = Year.now().getValue(); ClassRoom r = new ClassRoom("R1", "Phòng", 40);
            r.addDevice(new ProjectorRemote("D5", "5 năm", y - 5, BigDecimal.ONE));
            r.addDevice(new ProjectorRemote("D6", "6 năm", y - 6, BigDecimal.ONE));
            r.addDevice(new ProjectorRemote("DM", "Bảo trì", y, BigDecimal.ONE, DeviceStatus.UNDER_MAINTENANCE));
            check(r.getDevicesRequiringMaintenance().size() == 2, "Lọc sai mốc 5 năm");
        });
        test("Kiểm tra dữ liệu thiết bị", () -> {
            expect(IllegalArgumentException.class, () -> mic(" "));
            expect(IllegalArgumentException.class, () -> new ProjectorRemote("D", "D", 2020, BigDecimal.ZERO));
            expect(IllegalArgumentException.class, () -> new ProjectorRemote("D", "D", Year.now().getValue() + 1, BigDecimal.ONE));
            expect(IllegalArgumentException.class, () -> new ProjectorRemote("D", "D", 2020, BigDecimal.ONE, DeviceStatus.BORROWED));
        });
        test("Thông tin phiếu không hợp lệ", () -> {
            expect(IllegalArgumentException.class, () -> new BorrowingSlip(" ", new Lecturer("G", "G"), "R", DAY));
            expect(NullPointerException.class, () -> new BorrowingSlip("P", null, "R", DAY));
            expect(IllegalArgumentException.class, () -> new BorrowingSlip("P", new Lecturer("G", "G"), " ", DAY));
        });
        test("Không bảo trì khi đang mượn", () -> {
            TeachingDevice d = mic("M"); BorrowingSlip s = slip(); s.addDevice(d, 1);
            expect(DeviceUnavailableException.class, d::startMaintenance);
            check(d.getStatus() == DeviceStatus.BORROWED, "Sai trạng thái");
            s.returnDevice("M", DAY); d.startMaintenance(); d.finishMaintenance();
            check(d.getStatus() == DeviceStatus.AVAILABLE, "Sai trạng thái sau bảo trì");
        });
        test("Ngày trả null giữ nguyên dữ liệu", () -> {
            BorrowingSlip s = slip(); TeachingDevice d = mic("M"); s.addDevice(d, 1);
            expect(NullPointerException.class, () -> s.returnDevice("M", null));
            check(!s.getItems().get(0).isReturned() && d.getStatus() == DeviceStatus.BORROWED, "Dữ liệu bị đổi");
        });
        test("Tràn ngày hạn trả không làm đổi thiết bị", () -> {
            BorrowingSlip s = new BorrowingSlip("P", new Lecturer("G", "G"), "R", LocalDate.MAX);
            TeachingDevice d = mic("M"); expect(java.time.DateTimeException.class, () -> s.addDevice(d, 1));
            check(s.getItems().isEmpty() && d.getStatus() == DeviceStatus.AVAILABLE, "Dữ liệu bị đổi");
        });
        System.out.println("RESULT: " + passed + " passed, " + failed + " failed");
        if (failed != 0) throw new AssertionError("Có kiểm thử thất bại.");
    }
}
