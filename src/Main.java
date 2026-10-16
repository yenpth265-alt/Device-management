package devicemanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Main {
    private final Scanner input = new Scanner(System.in);
    private final ClassRoom room = new ClassRoom("D9-201", "Phòng thực hành", 60);
    private final List<IBorrower> borrowers = new ArrayList<>();
    private final List<BorrowingSlip> slips = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--test")) { SelfTest.main(new String[0]); return; }
        if (args.length > 0 && args[0].equals("--demo")) { demo(); return; }
        new Main().run();
    }
    private Main() {
        int year = LocalDate.now().getYear();
        room.addDevice(new ProjectorRemote("MC01", "Điều khiển máy chiếu", year - 2, new BigDecimal("500000")));
        room.addDevice(new AirConditionerRemote("DH01", "Điều khiển điều hòa", year - 6, new BigDecimal("300000")));
        room.addDevice(new WirelessMicrophone("MIC01", "Micro không dây", year - 1, new BigDecimal("2000000")));
        room.addDevice(new AirConditionerRemote("DH02", "Điều khiển đang bảo trì", year, new BigDecimal("350000"), DeviceStatus.UNDER_MAINTENANCE));
        borrowers.add(new Lecturer("GV01", "Nguyễn Văn An"));
        borrowers.add(new TeachingAssistant("TG01", "Trần Thị Bình"));
    }
    private void run() {
        System.out.println("QUẢN LÝ THIẾT BỊ VÀ MƯỢN - TRẢ (Java)");
        System.out.println("Dữ liệu lưu trong RAM; ngày nhập theo yyyy-MM-dd.");
        while (true) {
            System.out.println("\n1. Danh sách thiết bị / người mượn\n2. Thêm thiết bị vào phòng\n3. Xóa thiết bị khỏi phòng\n4. Thêm người mượn\n5. Tạo phiếu mượn\n6. Thêm thiết bị vào phiếu\n7. Trả thiết bị\n8. Xem tất cả phiếu\n9. Phiếu trả muộn / chưa trả / quá hạn\n10. Bảo trì và chi phí dự kiến\n11. Đổi trạng thái bảo trì\n12. Chạy demo\n13. Chạy kiểm thử\n0. Thoát");
            try {
                String choice = read("Chọn: ");
                if (choice.equals("0")) return;
                switch (choice) {
                    case "1": room.getDevices().forEach(System.out::println); borrowers.forEach(System.out::println); break;
                    case "2": addDevice(); break;
                    case "3": removeDevice(); break;
                    case "4": addBorrower(); break;
                    case "5": createSlip(); break;
                    case "6": borrow(); break;
                    case "7": returnDevice(); break;
                    case "8": slips.forEach(Main::printSlip); break;
                    case "9": reports(); break;
                    case "10":
                        System.out.println("Chi phí bảo trì năm: " + room.calculateAnnualMaintenanceCost() + " VND");
                        System.out.println("Thiết bị cần bảo trì:"); room.getDevicesRequiringMaintenance().forEach(System.out::println); break;
                    case "11": maintenance(); break;
                    case "12": demo(); break;
                    case "13": SelfTest.main(new String[0]); break;
                    default: System.out.println("Lựa chọn không hợp lệ.");
                }
            } catch (BorrowingException ex) {
                System.out.println("Lỗi nghiệp vụ [" + ex.getClass().getSimpleName() + "]: " + ex.getMessage());
            } catch (DateTimeParseException ex) {
                System.out.println("Ngày không hợp lệ. Ví dụ: 2026-10-16.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                System.out.println("Lỗi: " + ex.getMessage());
            } finally {
                // Không thay đổi trạng thái thiết bị trong finally.
                System.out.println("--- Kết thúc lượt xử lý ---");
            }
        }
    }
    private String read(String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) return "0";
        return input.nextLine().trim();
    }
    private LocalDate date(String prompt) { return LocalDate.parse(read(prompt)); }
    private TeachingDevice findDevice(String id) {
        Device d = room.findDevice(id);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy thiết bị.");
        return (TeachingDevice) d;
    }
    private BorrowingSlip findSlip() {
        String id = read("Mã phiếu: ");
        return slips.stream().filter(s -> s.getSlipId().equalsIgnoreCase(id)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu."));
    }
    private void addDevice() {
        String type = read("Loại: 1-Điều hòa, 2-Micro, 3-Máy chiếu: ");
        String id = read("Mã thiết bị: "); String name = read("Tên thiết bị: ");
        int year = Integer.parseInt(read("Năm sử dụng: "));
        BigDecimal price = new BigDecimal(read("Giá mua: "));
        TeachingDevice d;
        switch (type) {
            case "1": d = new AirConditionerRemote(id, name, year, price); break;
            case "2": d = new WirelessMicrophone(id, name, year, price); break;
            case "3": d = new ProjectorRemote(id, name, year, price); break;
            default: throw new IllegalArgumentException("Loại thiết bị không hợp lệ.");
        }
        // Giữ mã duy nhất cả với thiết bị đã rời phòng nhưng còn trong lịch sử phiếu.
        if (slips.stream().flatMap(s -> s.getItems().stream()).anyMatch(i -> i.getDevice().getDeviceId().equalsIgnoreCase(d.getDeviceId())))
            throw new IllegalArgumentException("Mã thiết bị đã tồn tại trong lịch sử mượn.");
        room.addDevice(d); System.out.println("Đã thêm thiết bị.");
    }
    private void removeDevice() {
        TeachingDevice d = findDevice(read("Mã thiết bị cần xóa khỏi phòng: "));
        if (d.getStatus() == DeviceStatus.BORROWED) throw new IllegalStateException("Hãy trả thiết bị trước khi xóa khỏi phòng.");
        room.removeDevice(d.getDeviceId()); System.out.println("Đã xóa khỏi phòng; lịch sử phiếu vẫn giữ tham chiếu thiết bị.");
    }
    private void addBorrower() {
        String type = read("1-Giảng viên, 2-Trợ giảng: ");
        String id = read("Mã người mượn: "); String name = read("Họ tên: ");
        if (borrowers.stream().anyMatch(b -> b.getBorrowerId().equalsIgnoreCase(id))) throw new IllegalArgumentException("Trùng mã người mượn.");
        if (type.equals("1")) borrowers.add(new Lecturer(id, name));
        else if (type.equals("2")) borrowers.add(new TeachingAssistant(id, name));
        else throw new IllegalArgumentException("Loại người mượn không hợp lệ.");
    }
    private void createSlip() {
        String id = read("Mã phiếu: ");
        if (slips.stream().anyMatch(s -> s.getSlipId().equalsIgnoreCase(id))) throw new IllegalArgumentException("Trùng mã phiếu.");
        String borrowerId = read("Mã người mượn (GV01/TG01 hoặc mã đã tạo): ");
        IBorrower b = borrowers.stream().filter(x -> x.getBorrowerId().equalsIgnoreCase(borrowerId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người mượn."));
        slips.add(new BorrowingSlip(id, b, read("Phòng dạy: "), date("Ngày mượn: ")));
        System.out.println("Đã tạo phiếu.");
    }
    private void borrow() {
        BorrowingSlip s = findSlip(); TeachingDevice d = findDevice(read("Mã thiết bị: "));
        s.addDevice(d, Integer.parseInt(read("Số ngày mượn: "))); System.out.println("Mượn thành công.");
    }
    private void returnDevice() {
        BorrowingSlip s = findSlip();
        BigDecimal fee = s.returnDevice(read("Mã thiết bị: "), date("Ngày trả: "));
        System.out.println("Trả thành công. Phí muộn: " + fee + " VND");
    }
    private void maintenance() {
        TeachingDevice d = findDevice(read("Mã thiết bị: "));
        String action = read("1-Bắt đầu bảo trì, 2-Kết thúc bảo trì: ");
        if (action.equals("1")) d.startMaintenance();
        else if (action.equals("2")) d.finishMaintenance();
        else throw new IllegalArgumentException("Lựa chọn không hợp lệ.");
        System.out.println(d);
    }
    private void reports() {
        LocalDate today = LocalDate.now();
        System.out.println("PHIẾU CÓ THIẾT BỊ ĐÃ TRẢ MUỘN:"); slips.stream().filter(BorrowingSlip::hasLateReturns).forEach(Main::printSlip);
        System.out.println("PHIẾU CHƯA TRẢ HẾT:"); slips.stream().filter(BorrowingSlip::hasUnreturnedItems).forEach(Main::printSlip);
        System.out.println("PHIẾU ĐANG QUÁ HẠN:"); slips.stream().filter(s -> s.isOverdue(today)).forEach(Main::printSlip);
    }
    private static void printSlip(BorrowingSlip s) {
        System.out.println(s); s.getItems().forEach(i -> System.out.println("  " + i));
        System.out.println("  Phí đã chốt: " + s.calculateTotalLateFee() + " VND");
    }
    private static void demo() {
        LocalDate day = LocalDate.now().minusDays(5); int year = day.getYear();
        Lecturer lecturer = new Lecturer("GV-DEMO", "Giảng viên demo");
        TeachingDevice remote = new ProjectorRemote("MC-DEMO", "Điều khiển máy chiếu", year, new BigDecimal("500000"));
        TeachingDevice mic = new WirelessMicrophone("MIC-DEMO", "Micro", year, new BigDecimal("2000000"));
        TeachingDevice ac = new AirConditionerRemote("DH-DEMO", "Điều khiển điều hòa", year, new BigDecimal("300000"));
        BorrowingSlip s = new BorrowingSlip("PM-DEMO", lecturer, "D9-201", day);
        s.addDevice(remote, 2); s.addDevice(mic, 1);
        s.returnDevice(remote.getDeviceId(), day.plusDays(2));
        s.returnDevice(mic.getDeviceId(), day.plusDays(3));
        BorrowingSlip pending = new BorrowingSlip("PM-CHUA-TRA", lecturer, "D9-202", day);
        pending.addDevice(ac, 1);
        System.out.println("DEMO: phiếu trả muộn (micro muộn 2 ngày, phí 40.000 VND)"); printSlip(s);
        System.out.println("DEMO: phiếu chưa trả, đang quá hạn"); printSlip(pending);
        System.out.println("Phí tạm tính phiếu chưa trả: " + pending.calculateEstimatedLateFee(LocalDate.now()) + " VND");
    }
}
