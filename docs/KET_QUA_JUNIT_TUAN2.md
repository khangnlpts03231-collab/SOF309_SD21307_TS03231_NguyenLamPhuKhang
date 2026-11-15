# Kết quả JUnit tuần 2

Ngày chạy: 15/11/2026. Môi trường: Linux, Temurin JDK 21.0.13, Maven 3.9.9, compile release 17.
Lệnh kiểm tra: `mvn test -Dtest=*ControllerTest` (môi trường soạn bài dùng offline sau khi tải đủ dependency).

| Lớp test | Số ca thực thi | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| AuthControllerTest | 51 | 0 | 0 | 0 |
| AdminCategoryControllerTest | 16 | 0 | 0 | 0 |
| Tổng | 67 | 0 | 0 | 0 |

**BUILD SUCCESS**. Đây là 67 lượt test gồm các dòng parameterized; không phải 67 phương thức Java riêng.
Báo cáo thực tế rút gọn được lưu tại minh-chung-junit/.

Đã thử demo đổi mong đợi mật khẩu 7 ký tự từ false thành true: 6 ca biên chạy, 1 ca thất bại đúng dự kiến. Đã khôi phục và chạy lại toàn bộ 67 ca thành công. Bản giao không chứa thay đổi cố ý làm đỏ.

Bộ test kiểm tra Controller và validation, DAO được mock; không xác nhận ghi SQL thật hay render JSP. Không chứng nhận các ca UI tuần 1 đã Pass. Chưa chạy trên Eclipse/Windows của sinh viên.

## Vấn đề gặp khi chuẩn bị và đã xử lý

- Cần JDK đầy đủ để compiler dùng release 17.
- Môi trường hạn chế tự attach agent: cấu hình Mockito javaagent trong Surefire.
- Lỗi test InvalidUseOfMatchersException: lấy giá trị request và tính hash trước lệnh verify, không gọi mock lồng trong lúc đang khai báo matcher.

Sinh viên tự chạy lại trên máy, lưu ảnh thật, bổ sung ngày chạy và các lỗi phát sinh nếu có.
