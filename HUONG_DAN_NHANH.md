# Tuần 2 hoàn chỉnh – Khang

1. Giải nén ra thư mục mới.
2. Nhấp đúp 01_KIEM_TRA_JUNIT.cmd để kiểm tra. Script tự tìm JDK 17/21, tải Maven chính thức nếu máy chưa có và chạy 67 ca. Cần Internet cho lần đầu tải thư viện.
3. Khi sẵn sàng nộp, nhấp đúp 02_DANG_GITHUB.cmd. Script chạy test lại; chỉ tiếp tục nếu đủ ca, không lỗi và không bỏ qua ca nào.
4. Nếu được hỏi đăng nhập GitHub, chọn khangnlpts03231-collab. Cần cài Git for Windows.
5. Khi thành công, trình duyệt mở commit; link nộp cũng được ghi trong LINK_NOP_TUAN2.txt.

Script tải bản repo mới vào một thư mục riêng, cập nhật file tuần 2 và báo cáo vừa chạy. Tài liệu tuần 1 và source ứng dụng trên GitHub được giữ nguyên. Nếu Git từ chối push, xem thông báo; bản clone được giữ lại và có thể chạy lại CMD để thử từ repo mới nhất. Không có force push.

Nếu máy cài JDK ở nơi riêng, đặt JAVA_HOME đến thư mục JDK đó. Script không sửa PATH hoặc JAVA_HOME toàn hệ thống. Trong Eclipse chọn JDK 21 tại tab JRE của cấu hình chạy.

Đã sửa tearDown để không phát sinh lỗi close() phụ nếu mock khởi tạo thất bại. Bộ dữ liệu nộp giữ 7,false; xem docs/DEMO_TRUOC_THAY.md để trình diễn test đỏ rồi khôi phục.

Kết quả bạn đã gửi ngày 15/11/2026 lúc 16:08:15 +07:00: 67 ca, 0 Failure/Error/Skipped, BUILD SUCCESS. Bản chỉnh lần này cần chạy lại trên máy qua CMD; chỉ log mới được script đăng làm minh chứng. Unit test dùng DAO giả, chưa chứng minh SQL/JSP thật hoạt động.
