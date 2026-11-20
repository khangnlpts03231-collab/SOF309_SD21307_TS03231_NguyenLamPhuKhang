# TEST PLAN — JUNIT CELINE CLOSET

| Thông tin | Nội dung |
|---|---|
| Sinh viên | Nguyễn Lâm Phú Khang |
| Mã sinh viên / lớp | TS03231 / SD21307 |
| Ngày cập nhật | 20/11/2026 |
| Mục tiêu | Kiểm tra xử lý tài khoản và CRUD danh mục tại Controller với dữ liệu đúng, sai, biên và ngoại lệ |
| Tổng số lượt | 79 lượt JUnit đã có implementation, gồm 63 Auth và 16 danh mục/phân quyền |
| Tài liệu ca kiểm thử | [TEST_CASES.md](TEST_CASES.md) |
| Cấu trúc mã | @BeforeEach, @AfterEach, @Test, @Tag, @DisplayName tiếng Việt. Một nhóm @ParameterizedTest cho 6 dữ liệu biên |

## 1. Phạm vi

| Chức năng | Đầu vào | Kết quả kiểm tra |
|---|---|---|
| Đăng ký | hoTen, email, soDienThoai, password, confirmPassword | Validation, chuẩn hóa email/điện thoại, hash mật khẩu, lời gọi register và tên trang JSP |
| Đăng nhập | email, password, next | Email tồn tại, kết quả DAO, session.auth, thông báo lỗi và trang chuyển theo CUSTOMER/ADMIN/STAFF/DELIVERY |
| Đăng xuất | GET /logout, session hiện tại | Hủy session, chuyển /home |
| Danh mục | action, id, maDM, tenDM, moTa, trangThai, edit | Gửi dữ liệu thêm/sửa/xóa/ẩn/hiện tới DAO, model danh sách và form sửa |
| Phân quyền | Session ADMIN, CUSTOMER, STAFF, DELIVERY hoặc thiếu session | Cho phép ADMIN và chuyển vai trò khác về trang phù hợp |
| Ngoại lệ | SQLException từ DAO, ID sai kiểu | ServletException bọc đúng lỗi gốc, lời gọi DAO phù hợp |

JUnit xác nhận logic Controller thật với DAO và Servlet API giả lập. Lớp ControllerForTest ghi tên JSP vào page để assertion. Việc render JSP, truy vấn SQL và dữ liệu được ghi thật cần chạy kiểm thử web/tích hợp tương ứng.

## 2. Môi trường và dữ liệu

| Hạng mục | Cấu hình |
|---|---|
| Java | JDK 21 để chạy, compiler release 17 theo pom.xml |
| Framework | JUnit Jupiter 5.10.2, Mockito Core 5.14.2 |
| Maven | Surefire 3.2.5, Mockito javaagent trong pom.xml |
| Auth test | src/test/java/vn/celineclosset/controller/AuthControllerTest.java |
| Category test | src/test/java/vn/celineclosset/controller/AdminCategoryControllerTest.java |
| Fixture | Mock mới trước mỗi ca bằng setUp, đóng scope sau mỗi ca bằng tearDown |
| Timeout | 10 giây mỗi lượt theo @Timeout(10) |
| Context thử | /celine |
| Báo cáo | target/surefire-reports và docs/minh-chung-junit/style-2026-11-20 |

| Bộ dữ liệu | Giá trị cụ thể |
|---|---|
| Đăng ký chuẩn | QA Khang, qa@example.com, 0912345678, password=confirmPassword=Abcd123! |
| Mật khẩu biên | Ab1!aaa, Ab1!aaaa, Ab1!aaaaa và các chuỗi dài 31/32/33 ký tự ghi nguyên văn trong Test Cases |
| Đăng nhập khách | customer01@example.com với Khach123!, Khach123@, khach123!, chuỗi rỗng, null và dấu cách cuối |
| Tài khoản quản trị | admin01@example.com / Admin123!, vai trò ADMIN |
| Tài khoản nhân viên | staff01@example.com / Staff123!, vai trò STAFF |
| Tài khoản giao hàng | delivery01@example.com / Ship123!, vai trò DELIVERY |
| Tài khoản chưa có | unknown01@example.com, DAO.emailExists trả false |
| Email chuẩn hóa | CUSTOMER01@EXAMPLE.COM có dấu cách hai đầu, DAO nhận customer01@example.com |
| Danh mục thử | maDM=7 hoặc rỗng khi thêm, tenDM=QA-Khang, moTa=Test, trangThai=1 |
| ID lỗi | abc, chuỗi rỗng, 1.5, 999999999999999 |

Tài khoản trong bảng là dữ liệu thử được mô phỏng bằng DAO. SHA256(password) do PasswordUtil.hash tính. Mỗi ca tự đặt stub theo email và hash cụ thể. Cùng email được dùng lại trên các fixture độc lập.

## 3. Chiến lược và phân bổ ca

| Nhóm | ID | Số lượt | Kỹ thuật |
|---|---|---:|---|
| Độ dài mật khẩu | TC_JU_001–006 | 6 | Phân tích giá trị biên 7/8/9 và 31/32/33, @ParameterizedTest với @CsvSource |
| Quy tắc và thiếu mật khẩu | TC_JU_007–022 | 16 | Thiếu hoa/thường/số/ký tự đặc biệt, ký tự sai, dấu cách, null/rỗng |
| Điện thoại | TC_JU_023–031 | 9 | 9/10/11 số, đầu 0/1, chữ cái, dấu cách giữa/hai đầu, null/rỗng |
| Email, trùng, xác nhận và lỗi SQL | TC_JU_032–043 | 12 | Định dạng, chuẩn hóa, thiếu, trùng, xác nhận sai và assertThrows |
| Đăng nhập và đăng xuất cơ bản | TC_JU_044–051 | 8 | 4 vai trò, wishlist, email chưa có, DAO trả null, hủy session |
| CRUD, quyền và ngoại lệ danh mục | TC_JU_052–067 | 16 | Thêm/sửa/xem/xóa/ẩn/hiện, quyền, ID sai và lỗi SQL |
| Dữ liệu đăng nhập mở rộng | TC_LOGIN_001–012 | 12 | Nhiều email/mật khẩu cụ thể, đúng/sai/rỗng/null, chuẩn hóa và vai trò |
| **Tổng** | | **79** | **63 Auth + 16 Category** |

Tag dùng để đọc/lọc nhóm. `smoke` đánh dấu ca luồng thành công, `negative` cho nhánh từ chối, `boundary` cho nhóm biên sáu dòng, `permission` cho quyền và `exception` cho ngoại lệ. Các ID và tên hàm tương ứng ghi trong Test Cases.

Mỗi test được viết theo ba bước có chú thích. Phần chuẩn bị đặt dữ liệu và mock, phần thực hiện gọi Controller, phần kiểm tra dùng assertEquals/assertSame/assertThrows và Mockito verify. Tên hàm public và @DisplayName hiển thị ID cùng mô tả tiếng Việt trên cây kết quả.

## 4. Phân công và lịch thực hiện

| Nhân lực | Thời hạn dự kiến | Ngày bắt đầu | Ngày bàn giao dự kiến | Phương thức | Phạm vi và nhiệm vụ |
|---|---|---|---|---|---|
| Nguyễn Lâm Phú Khang | 30 phút | 20/11/2026 | 20/11/2026 | Đọc mã và đối chiếu | Xem đặc tả, tên trường request, quy tắc validation, vai trò và thao tác danh mục |
| Nguyễn Lâm Phú Khang | 45 phút | 20/11/2026 | 20/11/2026 | Rà soát Test Cases | Đối chiếu 79 ID với dữ liệu và phương thức, đọc giải thích mock và kết quả cần assert |
| Nguyễn Lâm Phú Khang | 30 phút | 20/11/2026 | 20/11/2026 | Eclipse JUnit / Maven | Chạy nhóm biên trước, chạy đủ hai lớp, xem Failure Trace nếu có sai khác |
| Nguyễn Lâm Phú Khang | Khi có lỗi | Theo lần chạy | Sau khi xử lý | Sửa lỗi và hồi quy | Ghi dữ liệu tái hiện, expected/actual, xử lý nguyên nhân, chạy lại ca liên quan |
| Nguyễn Lâm Phú Khang | 15 phút | Sau lần chạy đạt | Cùng lần nộp | Report / GitHub | Lưu kết quả theo phiên bản mã, nộp link commit và trình bày một test từ chuẩn bị đến assertion |

Lịch trên là kế hoạch cá nhân. Kết quả thực thi được ghi theo bằng chứng của mỗi môi trường trong báo cáo JUnit.

## 5. Cách chạy và tiêu chí kết quả

1. Import Maven project vào Eclipse. Chọn JDK 21 cho Run Configuration, tải đủ thư viện.
2. Mở AuthControllerTest, chọn hàm cần xem và Run As → JUnit Test. Cây kết quả hiện ID/mô tả từ @DisplayName.
3. Chạy đủ hai lớp bằng lệnh sau tại thư mục chứa pom.xml.

```sh
mvn clean test -Dtest=AuthControllerTest,AdminCategoryControllerTest
```

4. Đọc tổng 79 lượt, failures/errors/skipped và BUILD SUCCESS. Đối chiếu tên invocation trong report.
5. Khi cần demo Failure, tạm đổi giá trị expected trong một assertion rồi chạy riêng ca đó. Ghi rõ đây là minh họa, khôi phục expected đúng yêu cầu và chạy lại trước khi nộp.

| Trạng thái | Ý nghĩa / bằng chứng |
|---|---|
| PASS | Các assertion/verify của ca đạt. Ghi report và thời gian |
| FAIL | Có khác biệt trong assertion hoặc lời gọi Mockito. Ghi expected/actual |
| ERROR | Lỗi thực thi ngoài ngoại lệ mong đợi, lỗi khởi tạo/môi trường. Giữ stack trace |
| SKIPPED | Ca bị bỏ qua. Ghi điều kiện hoặc annotation |

Ca đầu vào sai đạt khi hệ thống từ chối đúng mong đợi. assertThrows đạt khi nhận đúng ngoại lệ. Các ca mô tả xử lý ngoại lệ hiện tại cần được cập nhật khi thay đổi cách phản hồi nhập liệu.

## 6. Entry, Suspension, Resume và Exit

| Mốc | Tiêu chí |
|---|---|
| Bắt đầu | Project biên dịch, thư viện và JDK sẵn sàng, fixture có dữ liệu/stub đúng |
| Tạm dừng | Lỗi biên dịch, Maven, Mockito hoặc JDK làm test chưa thực thi được. Ghi lỗi môi trường |
| Chạy lại | Sửa nguyên nhân, chạy lại nhóm bị ảnh hưởng và giữ report mới |
| Hoàn thành tự động | 79 lượt, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS |
| Hoàn thành hồ sơ | Mỗi ca có ID, dữ liệu cụ thể, hàm tương ứng, mong đợi và bằng chứng theo phiên bản mã đã chạy |

## 7. Theo dõi giới hạn và lỗi

| Điểm cần theo dõi | Cách đánh giá |
|---|---|
| DAO trả null cho sai mật khẩu và tài khoản khóa | Unit xác nhận nhánh Controller chung. Kiểm thử DAO riêng để đánh giá truy vấn và trạng thái khóa |
| CRUD danh mục | Verify dữ liệu/ID/trạng thái được chuyển sang DAO. Đối chiếu SQL trong lần kiểm thử tích hợp |
| Render JSP | ControllerForTest ghi page. Kiểm tra giao diện trong lần chạy web |
| ID sai kiểu hiện gây ServletException | Giữ ca mô tả hiện trạng, cập nhật yêu cầu và test khi thêm xử lý lỗi thân thiện |
| Báo cáo coverage | Công bố tỷ lệ khi có lần đo bằng công cụ coverage và báo cáo cụ thể |

| ID lỗi | ID test | Phiên bản mã | Dữ liệu và bước tái hiện | Mong đợi | Thực tế | Mức độ | Retest | Bằng chứng |
|---|---|---|---|---|---|---|---|---|
| Ghi khi có lỗi | TC_JU_xxx / TC_LOGIN_xxx | Commit đang chạy | Chép đúng dữ liệu và phương thức | Theo Test Cases | Kết quả / trace | Theo ảnh hưởng | Đạt / chưa đạt | XML/TXT/ảnh |
