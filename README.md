# Quản lý thiết bị và mượn - trả (Java)

Project console cho lab tuần 06. Yêu cầu JDK 17 trở lên. Không cần Maven, Gradle, database hay thư viện ngoài.

## Chạy trên Windows

1. Giải nén ZIP.
2. Mở thư mục `DeviceManagementJava` bằng VS Code hoặc IntelliJ IDEA.
3. Mở terminal trong đúng thư mục có `run.bat`.
4. Chạy:

```bat
.\run.bat
```

Chạy demo hoặc bộ kiểm thử:

```bat
.\run.bat --demo
.\run.bat --test
```

Hoặc biên dịch và chạy thủ công (PowerShell/CMD):

```text
javac -encoding UTF-8 -d out src/devicemanagement/*.java
java -cp out devicemanagement.Main
java -cp out devicemanagement.Main --demo
java -cp out devicemanagement.SelfTest
```

Nếu `javac` không được nhận diện: cần cài JDK và cấu hình PATH trỏ đến thư mục bin của JDK. Không chỉ cài JRE. Trong IDE, chọn JDK 17+ và đánh dấu `src` là source root nếu IDE chưa nhận diện; chạy lớp `devicemanagement.Main`.

## Linux/macOS

```sh
sh run.sh
sh run.sh --demo
sh run.sh --test
```

## Các chức năng

- Xem/thêm thiết bị, xóa tham chiếu thiết bị khỏi phòng.
- Thêm người mượn (giảng viên/trợ giảng).
- Tạo phiếu, thêm thiết bị vào phiếu, trả thiết bị.
- Xem phiếu, lọc phiếu đã trả muộn/chưa trả hết/đang quá hạn.
- Tính phí muộn đã chốt và phí tạm tính.
- Tính chi phí bảo trì đa hình, lọc thiết bị cần bảo trì.
- Bắt đầu/kết thúc bảo trì.
- Demo và 23 ca kiểm thử tự động không phụ thuộc JUnit.

Dữ liệu khởi tạo: MC01, DH01, MIC01, DH02 (đang bảo trì); GV01 (tối đa 3 thiết bị), TG01 (tối đa 2). Menu 12 chạy demo với dữ liệu độc lập, không sửa dữ liệu menu. Dữ liệu chỉ nằm trong RAM và mất khi đóng chương trình.

## Quyết định thiết kế để xử lý chỗ chưa thống nhất trong đề

1. **Mượn theo ngày**, không theo tiết: đề trộn `fromPeriod/toPeriod` với `borrowDays`, `DueDate` và kiểm thử trả muộn 2 ngày. Bản này chọn nghiệp vụ theo ngày của mục 9 và 11; không thêm trường tiết không được sử dụng. Nếu giảng viên yêu cầu theo tiết thì cần thay đổi mô hình thời gian và cách tính phí, không chỉ đổi tên biến.
2. `LocalDate` biểu diễn ngày; hạn trả = ngày mượn + số ngày. Trả đúng ngày hạn không phạt. Ví dụ mượn 2026-10-16 trong 1 ngày, hạn trả 2026-10-17; trả 2026-10-19 là muộn 2 ngày. Cho phép nhập ngày lịch sử/tương lai để mô phỏng; chỉ cấm trả trước ngày mượn.
3. Dùng `AVAILABLE`, `BORROWED`, `UNDER_MAINTENANCE` theo nghiệp vụ mượn–trả. Không dùng enum `Active/Retired` mâu thuẫn ở phần sau đề.
4. `TeachingDevice` kế thừa `Device`; ba lớp thiết bị cụ thể kế thừa `TeachingDevice`.
5. Java dùng `BigDecimal` thay cho `decimal` và phương thức `returnDevice` thay cho `Return` (`return` là từ khóa). Các phương thức dùng camelCase theo quy ước Java.
6. `BorrowingException` kế thừa `RuntimeException`. Các lỗi nghiệp vụ có ngoại lệ riêng; dữ liệu rỗng/sai dùng `IllegalArgumentException`, null dùng `NullPointerException` qua `Objects.requireNonNull`.
7. Mã thiết bị không đổi; so sánh mã không phân biệt hoa/thường. Ứng dụng dùng chung đối tượng thiết bị giữa phòng và phiếu; không tạo bản sao cùng mã để cho mượn. Phần UI giữ mã thiết bị duy nhất cả trong lịch sử phiếu.
8. Giới hạn 3/2 là tổng item trên một phiếu, kể cả đã trả. Không giới hạn tổng trên nhiều phiếu vì đề chưa yêu cầu. Thiết bị đã trả được mượn lại trên phiếu mới.
9. `calculateTotalLateFee()` chỉ cộng phí của thiết bị đã trả; `calculateEstimatedLateFee(asOf)` cộng phí đã chốt và phí tạm tính của thiết bị chưa trả tại ngày chỉ định. Chưa triển khai thu tiền.
10. `ClassRoom` kết tập thiết bị. Xóa thiết bị khỏi phòng không xóa đối tượng đang được phiếu tham chiếu. UI yêu cầu trả trước khi xóa khỏi phòng để tránh mất khả năng chọn thiết bị.
11. `BorrowingSlip` hợp thành `BorrowItem`: constructor item ở mức package-private; bên ngoài nhận danh sách chỉ đọc. Người mượn và thiết bị tồn tại độc lập với phiếu. Không có tính năng xóa phiếu đang mượn.
12. Trạng thái được bảo vệ bằng private field; thay đổi mượn/trả qua các phương thức nội bộ. Mọi kiểm tra nghiệp vụ chạy trước cập nhật. Đây là chương trình đơn luồng; không tuyên bố bảo đảm giao dịch đa luồng/database.
13. Chưa triển khai hợp đồng bảo trì: tiêu đề có nhắc nhưng nội dung không quy định lớp hay nghiệp vụ hợp đồng.

## Các giá trị giả định (không phải số liệu được đề cung cấp)

| Loại thiết bị | Giới hạn mượn | Phạt/ngày muộn | Bảo trì/năm |
| --- | --- | --- | --- |
| Điều khiển điều hòa | 2 ngày (giả định) | 10.000 VND | 5% giá mua |
| Micro không dây | 1 ngày (theo tình huống đề) | 20.000 VND | 10% giá mua |
| Điều khiển máy chiếu | 2 ngày (giả định) | 10.000 VND | 7% giá mua |

Phí muộn = max(0, số ngày từ hạn trả đến ngày trả) × đơn giá/ngày. Bảo trì làm tròn 2 chữ số thập phân HALF_UP. Muốn đổi giới hạn, đơn giá hoặc tỷ lệ: sửa lớp loại thiết bị tương ứng và cập nhật kỳ vọng kiểm thử.

## Cấu trúc mã nguồn

Tất cả file Java nằm trong `src/devicemanagement`, mỗi lớp public một file:

- `DeviceStatus`, `Device`, `TeachingDevice`: enum và lớp trừu tượng.
- `AirConditionerRemote`, `WirelessMicrophone`, `ProjectorRemote`: ba loại thiết bị.
- `IBorrower`, `Borrower`, `Lecturer`, `TeachingAssistant`: người mượn/interface.
- `ClassRoom`: kết tập thiết bị, tính phí bảo trì đa hình.
- `BorrowItem`, `BorrowingSlip`: chi tiết và phiếu mượn.
- `BorrowingException` và 7 ngoại lệ con: sáu ngoại lệ bắt buộc + `InvalidReturnDateException`.
- `Validation`: kiểm tra chuỗi dùng chung.
- `Main`: menu, try/catch/finally và demo.
- `SelfTest`: 23 ca kiểm thử, bao gồm đủ bảy tình huống lỗi của đề và tính nhất quán sau lỗi.

## Kết quả kiểm tra bản bàn giao

Đã biên dịch bằng trình biên dịch Java 17 và chạy bộ kiểm thử: **23 passed, 0 failed**. Xem `TEST_RESULTS.txt`. Không yêu cầu bật `-ea`: bộ kiểm thử tự kiểm tra điều kiện và ném AssertionError khi thất bại.
