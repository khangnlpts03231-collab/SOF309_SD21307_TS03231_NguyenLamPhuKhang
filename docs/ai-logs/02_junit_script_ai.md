# Nhật ký AI – JUnit tuần 2

Ngày chuẩn bị: 15/11/2026. Sinh viên: Nguyễn Lâm Phú Khang, TS03231, SD21307.

## Yêu cầu người học

Muốn có nhiều ca dữ liệu đúng/sai, hiểu được cách viết và chạy từ đầu đến cuối để demo trước giảng viên. Gửi Celine_Closet_Tuan1.zip làm đầu vào.

## Chỉ dẫn thiết kế đã áp dụng trong phiên hỗ trợ

Đọc AuthController, AdminCategoryController, BaseController, ValidationUtil và DAO liên quan. Viết JUnit 5 gọi Controller thật; dùng Mockito cô lập DAO, request/response/session. Có ParameterizedTest + CsvSource cho biên mật khẩu/điện thoại, assertThrows cho ngoại lệ thực tế và Timeout. Không sửa nghiệp vụ để tạo kết quả mong muốn. Viết hướng dẫn demo đỏ/xanh và nêu phạm vi mock.

## Kết quả và kiểm tra cần làm

Tạo AuthControllerTest và AdminCategoryControllerTest; thêm Mockito test scope. Xem KET_QUA_JUNIT_TUAN2.md để biết kết quả chạy tại môi trường soạn bài. Sinh viên cần tự chạy lại trên Eclipse và bổ sung ảnh, ngày chạy, lỗi đã gặp cùng cách xử lý. Đây là phần AI hỗ trợ, không phải xác nhận sinh viên đã tự chạy.

## Quyết định sau đối chiếu mã nguồn

Không dùng ví dụ DuplicateUserException của dự án khác: email trùng được báo trên form. Giữ nguyên source main; mock construction vì DAO đang được new trực tiếp. Chỉ thay view trong lớp test để không kéo truy vấn dữ liệu header vào test Controller.

## Kết quả cuối phiên

67 lượt test Pass, 0 Failure/Error/Skipped trên Linux JDK 21. Thử mong đợi sai ở biên 7 ký tự tạo 1 Failure; đã khôi phục và chạy xanh. Đã sửa lỗi matcher trong test và thêm Mockito javaagent cho môi trường hạn chế attach. Sinh viên cần chạy lại trên máy cá nhân.


## Cập nhật 20/11/2026 theo ảnh mẫu giảng viên

Yêu cầu của sinh viên là viết test đơn giản, dễ đọc, tên hàm public, @Tag và @DisplayName tiếng Việt, có dữ liệu cụ thể và assertion rõ. Sinh viên yêu cầu đăng trực tiếp code cùng Test Plan và Test Cases lên GitHub.

Đã viết lại hai lớp theo ba bước chuẩn bị, thực hiện và kiểm tra. Giữ 67 ca gốc, triển khai thêm 12 ca đăng nhập. Nhóm biên giữ @ParameterizedTest/@CsvSource. Cập nhật bảng cho đủ 79 lượt và tên phương thức thực tế. Chạy thành công 79 ca, 0 failures, 0 errors, 0 skipped. Xem KET_QUA_JUNIT_TUAN2.md.
