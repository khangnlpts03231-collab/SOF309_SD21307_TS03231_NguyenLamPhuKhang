# Demo JUnit theo thứ tự

1. Mở TEST_PLAN.md và TEST_CASES.md. Chỉ ra ID, dữ liệu và mong đợi của một ca.
2. Mở AuthControllerTest. Giải thích setUp tạo Controller và request/response/session. AccountDAO được mock vì Controller tự khởi tạo DAO. tearDown đóng scope sau ca.
3. Mở `testDangKy_passwordRules_012`. Ca dùng QA Khang, qa@example.com, 0912345678, Abcd123! và xác nhận trùng. Đọc lần lượt ba phần chuẩn bị, gọi doPost và kiểm tra. Assert trang login.jsp, verify dữ liệu băm và thông báo thành công.
4. Chạy riêng ca qua Run As → JUnit Test. @DisplayName hiển thị TC_JU_012 bằng tiếng Việt. @Tag("smoke") gắn nhóm ca.
5. Nếu cần minh họa Failure, trong chính ca đó đổi tạm `assertEquals("login.jsp", controller.page, ...)` thành `assertEquals("register.jsp", controller.page, ...)`. Chạy riêng, xem expected/actual. Đây là cố ý đổi kỳ vọng để học. Khôi phục login.jsp và chạy xanh trước khi nộp.
6. Chạy `testDangKy_BienDoDaiMatKhau`. Mỗi dòng @CsvSource là một bộ dữ liệu. Chuỗi 7/33 ký tự được từ chối, chuỗi 8/9/31/32 được chấp nhận. Dữ liệu bị từ chối đúng yêu cầu vẫn cho test xanh.
7. Mở `testDangNhap_DuLieuCuThe_002`. So sánh mật khẩu Khach123@ của ca sai với Khach123! của ca đúng. DAO nhận hash đúng mật khẩu thử rồi trả null, test kiểm tra thông báo và session.
8. Mở AdminCategoryControllerTest. Chọn `testDanhMuc_addOrUpdateDelegatesExactForm_055` để xem form thêm, `testDanhMuc_malformedIdThrowsServletException_063` để xem assertThrows.
9. Chạy đủ hai lớp bằng Maven Goals `clean test -Dtest=AuthControllerTest,AdminCategoryControllerTest`. Kỳ vọng 79/0/0/0 và BUILD SUCCESS. Report nằm trong target/surefire-reports.

Các test xác nhận Controller với DAO giả lập và tên JSP được ghi nhận. Khi thầy yêu cầu bằng chứng web, thực hiện thao tác trên Tomcat và SQL Server thử rồi giữ kết quả riêng.
