# Kết quả JUnit Celine Closet — bản trình bày theo mẫu giảng viên

Ngày chạy 20/11/2026, hoàn tất 15:41:35 giờ Việt Nam. Linux, Temurin JDK 21.0.13, Maven 3.9.9, compiler release 17.

| Lớp | Số lượt | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| AuthControllerTest | 63 | 0 | 0 | 0 |
| AdminCategoryControllerTest | 16 | 0 | 0 | 0 |
| Tổng | 79 | 0 | 0 | 0 |

**BUILD SUCCESS.** Chạy thực tế bằng `mvn clean test -Dtest=AuthControllerTest,AdminCategoryControllerTest`.

Mã test dùng @BeforeEach/@AfterEach, các phương thức public, @Tag và @DisplayName có ID/tiếng Việt. Dữ liệu từng ca được đặt ở đầu phương thức, tiếp theo là gọi Controller và kiểm tra kết quả. Nhóm sáu biên mật khẩu dùng @ParameterizedTest với @CsvSource.

Đã triển khai 12 ca đăng nhập bổ sung với nhiều email/mật khẩu cụ thể. Test Cases và Test Plan đồng bộ tổng 79 lượt, tên phương thức, fixture và kết quả.

Báo cáo Surefire được giữ trong [style-2026-11-20](minh-chung-junit/style-2026-11-20). Lần chạy dùng DAO mock để kiểm tra Controller. Chạy web/SQL cần môi trường và bằng chứng riêng. Sinh viên tự chạy lại trên Eclipse/Windows và ghi kết quả máy mình.
