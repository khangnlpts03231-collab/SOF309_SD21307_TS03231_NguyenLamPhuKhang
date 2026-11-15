# Demo từ đầu đến cuối trước thầy

## A. Tự viết một ca đơn giản (3 phút)

Nói: “Em kiểm tra quy tắc mật khẩu của chức năng đăng ký: 8–32 ký tự, đủ chữ hoa, chữ thường, số và ký tự đặc biệt.”
Mở src/main/java/vn/celineclosset/util/ValidationUtil.java → isValidPassword để chỉ code thật.
Trong src/test/java, tạo package vn.celineclosset.demo, tạo class BuoiDemoTest rồi gõ:

```java
package vn.celineclosset.demo;
import org.junit.jupiter.api.Test;
import vn.celineclosset.util.ValidationUtil;
import static org.junit.jupiter.api.Assertions.*;

class BuoiDemoTest {
    @Test
    void matKhau7KyTuBiTuChoi() {
        String password = "Ab1!aaa"; // 7 ký tự
        boolean actual = ValidationUtil.isValidPassword(password);
        assertFalse(actual); // Mong đợi: không hợp lệ
    }
}
```

Run As → JUnit Test. Nói: “Arrange là chuẩn bị password, Act là gọi hàm thật, Assert là so sánh kết quả với mong đợi.”
Đây là test hàm validation để học, chưa phải test Controller. Bước C sẽ gọi Controller thật.

## B. Chủ động demo đỏ rồi sửa xanh

Đổi assertFalse(actual) thành assertTrue(actual), lưu và chạy lại. Test phải đỏ vì chuỗi 7 ký tự bị hàm thật từ chối trong khi em cố ý yêu cầu true.
Mở Failure Trace: Expected true, actual false. Nói rõ: “Em cố ý sửa mong đợi để minh họa JUnit phát hiện sự khác biệt; đây không phải lỗi nghiệp vụ mới.”
Đổi lại assertFalse(actual), chạy xanh. Không nộp bản cố ý sai.

## C. Chạy nhiều ca trên Controller

Mở AuthControllerTest.passwordBoundaries. Giải thích:
- @ParameterizedTest chạy cùng hàm với nhiều dòng dữ liệu.
- @CsvSource có 6 dòng 7/8/9 và 31/32/33.
- password() đặt password và confirmPassword giống nhau.
- controller.doPost(req, resp) gọi xử lý thật.
- verify(dao).register(...) xác nhận ca hợp lệ gọi lưu đúng dữ liệu đã hash.
- verify(..., never()).register(...) xác nhận dữ liệu sai không gọi lưu.
- Kiểm tra tên JSP và thuộc tính error/success để biết phản hồi đúng.

Chạy phương thức. Nếu muốn tạo đỏ ở chính Controller test: tạm đổi dòng "7,false" thành "7,true", chạy và quan sát Wanted but not invoked (không gọi register như mong đợi sai). Đổi lại false rồi chạy lại.

## D. Giải thích mock

“Em giả lập database qua DAO, không giả lập hàm đăng ký của Controller. Mock giúp test không phụ thuộc database và không tạo tài khoản thật. Hàm view chỉ ghi nhận trang sẽ mở; test này không kiểm tra render JSP.”
Mở setUp và tearDown. Mỗi lần chạy tạo mock mới, cuối ca đóng mockConstruction, nên các bộ dữ liệu không dùng chung trạng thái.

## E. Ngoại lệ và timeout

Mở databaseErrorIsWrappedAsServletException:
- thenThrow mô phỏng DAO gặp SQLException.
- assertThrows yêu cầu Controller ném ServletException.
- assertSame xác nhận giữ đúng nguyên nhân gốc.
Đăng ký trùng email được kiểm tra riêng bằng thông báo error vì source hiện tại không ném DuplicateUserException.

@Timeout(10) giới hạn mỗi ca ở 10 giây để bắt ca bị treo; không phải cam kết hiệu năng web. Nếu thầy yêu cầu đúng 2 giây, đổi thành 2 và chạy lại trên máy; lần đầu JVM/mock có thể chậm.

## F. CRUD và phân quyền

Mở AdminCategoryControllerTest. Chạy changeStatus, addOrUpdateDelegatesExactForm, nonAdminCannotReadOrWrite.
Nói: “Em kiểm tra ADMIN được gọi DAO; CUSTOMER/STAFF/DELIVERY bị chuyển hướng và không được gọi DAO danh mục.”
Các test này không chứng minh bản ghi thật trong SQL đã đổi; việc đó cần integration test hoặc test thủ công.

## G. Chốt buổi demo

Chạy Maven Goals: test -Dtest=*ControllerTest.
Mở Console, đọc tổng số test, Failures/Errors. Lưu ảnh thật và ghi ngày chạy. Nếu đỏ, đọc Failure Trace trước; không đổi mong đợi chỉ để xanh nếu chưa đối chiếu yêu cầu.

## Những khoảng trống cần biết khi thầy hỏi

- Kiểm tra tên danh mục rỗng ở bản tuần 1 nằm ở HTML required; Controller hiện gọi DAO trực tiếp. Bộ này không khẳng định backend đã chặn tên rỗng.
- Mã danh mục không phải số hiện gây ServletException; test mô tả hiện trạng, không khẳng định đây là thông báo lỗi thân thiện.
- Login DAO trả null cho cả sai mật khẩu và tài khoản khóa. Unit test xác nhận nhánh chung, không xác minh câu SQL lọc trạng thái.
- Phạm vi là hai Controller trọng tâm, không phải tất cả phân hệ của Celine Closet.
