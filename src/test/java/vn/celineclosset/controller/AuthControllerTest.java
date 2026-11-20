package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;
import vn.celineclosset.dao.AccountDAO;
import vn.celineclosset.util.PasswordUtil;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Celine Closet - Tài khoản")
@Timeout(10)
public class AuthControllerTest {

    private AccountDAO accountDAO;
    private AuthControllerForTest controller;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private MockedConstruction<AccountDAO> accountScope;

    // Ghi nhận trang sẽ mở để kiểm tra kết quả xử lý của Controller.
    private static class AuthControllerForTest extends AuthController {
        private String page;

        @Override
        protected void view(HttpServletRequest request,
                            HttpServletResponse response, String jsp) {
            page = jsp;
        }
    }

    @BeforeEach
    public void setUp() {
        // Controller tự new DAO. Mockito thay DAO bằng đối tượng giả lập.
        accountScope = mockConstruction(AccountDAO.class);
        controller = new AuthControllerForTest();
        accountDAO = accountScope.constructed().get(0);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/celine");
        when(request.getServletPath()).thenReturn("/register");
        when(request.getParameter("hoTen")).thenReturn("QA Khang");
        when(request.getParameter("email")).thenReturn("qa@example.com");
        when(request.getParameter("soDienThoai")).thenReturn("0912345678");
        when(request.getParameter("password")).thenReturn("Abcd123!");
        when(request.getParameter("confirmPassword")).thenReturn("Abcd123!");
    }

    @AfterEach
    public void tearDown() {
        if (accountScope != null) {
            accountScope.close();
        }
    }

    @ParameterizedTest(name = "{0} - Mật khẩu {1}, hợp lệ = {2}")
    @CsvSource({
        "TC_JU_001, Ab1!aaa, false",
        "TC_JU_002, Ab1!aaaa, true",
        "TC_JU_003, Ab1!aaaaa, true",
        "TC_JU_004, Ab1!aaaaaaaaaaaaaaaaaaaaaaaaaaa, true",
        "TC_JU_005, Ab1!aaaaaaaaaaaaaaaaaaaaaaaaaaaa, true",
        "TC_JU_006, Ab1!aaaaaaaaaaaaaaaaaaaaaaaaaaaaa, false"
    })
    @Tag("boundary")
    @DisplayName("TC_JU_001-006 - Kiểm tra biên độ dài mật khẩu")
    public void testDangKy_BienDoDaiMatKhau(String testCaseId,
                                         String password,
                                         boolean expectedValid) throws Exception {
        // 1. Chuẩn bị dữ liệu
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(password);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        if (expectedValid) {
            String expectedHash = PasswordUtil.hash(password);
            verify(accountDAO).register("QA Khang", "qa@example.com",
                    expectedHash, "0912345678");
            assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
            verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
            verify(request, never()).setAttribute(eq("error"), any());
        } else {
            assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
            verify(request).setAttribute("error", "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.");
            verify(accountDAO, never()).register(any(), any(), any(), any());
            verify(response, never()).sendRedirect(anyString());
        }
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_007 - Thiếu chữ hoa")
    public void testDangKy_passwordRules_007() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "abcdefgh1!";
        String confirmPassword = "abcdefgh1!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_008 - Thiếu chữ thường")
    public void testDangKy_passwordRules_008() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "ABCDEFGH1!";
        String confirmPassword = "ABCDEFGH1!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_009 - Thiếu số")
    public void testDangKy_passwordRules_009() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcdefgh!";
        String confirmPassword = "Abcdefgh!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_010 - Thiếu ký tự đặc biệt")
    public void testDangKy_passwordRules_010() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcdefg12";
        String confirmPassword = "Abcdefg12";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_011 - Dấu ? ngoài tập cho phép")
    public void testDangKy_passwordRules_011() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123?";
        String confirmPassword = "Abcd123?";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_012 - Chấp nhận ký tự !")
    public void testDangKy_passwordRules_012() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_013 - Chấp nhận ký tự @")
    public void testDangKy_passwordRules_013() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123@";
        String confirmPassword = "Abcd123@";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_014 - Chấp nhận ký tự #")
    public void testDangKy_passwordRules_014() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123#";
        String confirmPassword = "Abcd123#";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_015 - Chấp nhận ký tự $")
    public void testDangKy_passwordRules_015() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123$";
        String confirmPassword = "Abcd123$";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_016 - Chấp nhận ký tự %")
    public void testDangKy_passwordRules_016() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123%";
        String confirmPassword = "Abcd123%";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_017 - Chấp nhận ký tự ^")
    public void testDangKy_passwordRules_017() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123^";
        String confirmPassword = "Abcd123^";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_018 - Chấp nhận ký tự &")
    public void testDangKy_passwordRules_018() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123&";
        String confirmPassword = "Abcd123&";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_019 - Chấp nhận ký tự *")
    public void testDangKy_passwordRules_019() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123*";
        String confirmPassword = "Abcd123*";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_020 - Khoảng trắng bên trong mật khẩu")
    public void testDangKy_passwordRules_020() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd 123!";
        String confirmPassword = "Abcd 123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_021 - Mật khẩu thiếu")
    public void testDangKy_missingPassword_021() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = null;
        String confirmPassword = null;

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_022 - Mật khẩu chuỗi rỗng")
    public void testDangKy_missingPassword_022() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "";
        String confirmPassword = "";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_023 - Điện thoại 9 chữ số")
    public void testDangKy_phoneRules_023() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "091234567";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_024 - Điện thoại 10 chữ số, đầu 0")
    public void testDangKy_phoneRules_024() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_025 - Điện thoại 11 chữ số")
    public void testDangKy_phoneRules_025() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "09123456789";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_026 - Điện thoại 10 chữ số, đầu 1")
    public void testDangKy_phoneRules_026() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "1912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_027 - Điện thoại Có chữ a")
    public void testDangKy_phoneRules_027() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "091234567a";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_028 - Điện thoại Có dấu cách ở giữa")
    public void testDangKy_phoneRules_028() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "091 2345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_029 - Điện thoại Có dấu cách hai đầu")
    public void testDangKy_phoneRules_029() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = " 0912345678 ";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_030 - Thiếu điện thoại")
    public void testDangKy_missingPhone_030() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = null;
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_031 - Điện thoại chuỗi rỗng")
    public void testDangKy_missingPhone_031() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_032 - Email sai định dạng abc")
    public void testDangKy_invalidEmail_032() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "abc";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
        verifyNoInteractions(accountDAO);
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_033 - Email sai định dạng @mail.com")
    public void testDangKy_invalidEmail_033() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "@mail.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
        verifyNoInteractions(accountDAO);
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_034 - Email sai định dạng a@@mail.com")
    public void testDangKy_invalidEmail_034() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "a@@mail.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
        verifyNoInteractions(accountDAO);
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_035 - Email sai định dạng a@b")
    public void testDangKy_invalidEmail_035() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "a@b";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
        verifyNoInteractions(accountDAO);
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_036 - Email sai định dạng a@b.")
    public void testDangKy_invalidEmail_036() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "a@b.";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
        verifyNoInteractions(accountDAO);
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_037 - Email sai định dạng a b@mail.com")
    public void testDangKy_invalidEmail_037() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "a b@mail.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
        verifyNoInteractions(accountDAO);
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_038 - Thiếu email")
    public void testDangKy_missingEmail_038() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = null;
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_039 - Email chuỗi rỗng")
    public void testDangKy_missingEmail_039() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Dữ liệu sai phải giữ trang đăng ký");
        verify(request).setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_040 - Trim và chuyển email sang chữ thường")
    public void testDangKy_normalizeEmailBeforeSaving_040() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "  QA@EXAMPLE.COM  ";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        String expectedHash = PasswordUtil.hash(password);
        verify(accountDAO).register("QA Khang", "qa@example.com",
                expectedHash, "0912345678");
        assertEquals("login.jsp", controller.page, "Đăng ký đúng phải mở trang đăng nhập");
        verify(request).setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        verify(request, never()).setAttribute(eq("error"), any());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_041 - Từ chối email đã tồn tại")
    public void testDangKy_duplicateEmailIsRejected_041() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);
        when(accountDAO.emailExists(email)).thenReturn(true);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Đăng ký sai phải giữ form");
        verify(request).setAttribute("error", "Email đã tồn tại.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_042 - Xác nhận mật khẩu khác nhau")
    public void testDangKy_confirmationMismatchIsRejected_042() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Khac123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);

        // 2. Gọi chức năng cần kiểm tra
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("register.jsp", controller.page, "Đăng ký sai phải giữ form");
        verify(request).setAttribute("error", "Mật khẩu nhập lại không khớp.");
        verify(accountDAO, never()).register(any(), any(), any(), any());
    }

    @Test
    @Tag("exception")
    @DisplayName("TC_JU_043 - Bọc lỗi SQL khi kiểm tra email")
    public void testDangKy_databaseErrorIsWrappedAsServletException_043() throws Exception {
        // 1. Chuẩn bị dữ liệu
        String hoTen = "QA Khang";
        String email = "qa@example.com";
        String soDienThoai = "0912345678";
        String password = "Abcd123!";
        String confirmPassword = "Abcd123!";

        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("soDienThoai")).thenReturn(soDienThoai);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("confirmPassword")).thenReturn(confirmPassword);
        SQLException databaseError = new SQLException("Database unavailable");
        when(accountDAO.emailExists(email)).thenThrow(databaseError);

        // 2. Gọi Controller và bắt ngoại lệ mong đợi
        ServletException error = assertThrows(ServletException.class,
                () -> controller.doPost(request, response));

        // 3. Kiểm tra nguyên nhân
        assertSame(databaseError, error.getCause(), "Phải giữ nguyên lỗi SQL gốc");
        verify(accountDAO, never()).register(any(), any(), any(), any());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_044 - Đăng nhập thành công vai trò CUSTOMER")
    public void testTaiKhoan_loginRedirectsByRole_044() throws Exception {
        // 1. Chuẩn bị tài khoản
        String email = "qa@example.com";
        String password = "Abcd123!";
        Map<String, Object> user = Map.of("maTK", 1, "vaiTro", "CUSTOMER");

        when(request.getServletPath()).thenReturn("/login");
        when(accountDAO.emailExists(email)).thenReturn(true);
        when(accountDAO.login(email, PasswordUtil.hash(password))).thenReturn(user);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra phiên đăng nhập và trang đích
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/home");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_045 - Đăng nhập thành công vai trò ADMIN")
    public void testTaiKhoan_loginRedirectsByRole_045() throws Exception {
        // 1. Chuẩn bị tài khoản
        String email = "qa@example.com";
        String password = "Abcd123!";
        Map<String, Object> user = Map.of("maTK", 1, "vaiTro", "ADMIN");

        when(request.getServletPath()).thenReturn("/login");
        when(accountDAO.emailExists(email)).thenReturn(true);
        when(accountDAO.login(email, PasswordUtil.hash(password))).thenReturn(user);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra phiên đăng nhập và trang đích
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/admin/dashboard");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_046 - Đăng nhập thành công vai trò STAFF")
    public void testTaiKhoan_loginRedirectsByRole_046() throws Exception {
        // 1. Chuẩn bị tài khoản
        String email = "qa@example.com";
        String password = "Abcd123!";
        Map<String, Object> user = Map.of("maTK", 1, "vaiTro", "STAFF");

        when(request.getServletPath()).thenReturn("/login");
        when(accountDAO.emailExists(email)).thenReturn(true);
        when(accountDAO.login(email, PasswordUtil.hash(password))).thenReturn(user);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra phiên đăng nhập và trang đích
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/admin/orders");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_047 - Đăng nhập thành công vai trò DELIVERY")
    public void testTaiKhoan_loginRedirectsByRole_047() throws Exception {
        // 1. Chuẩn bị tài khoản
        String email = "qa@example.com";
        String password = "Abcd123!";
        Map<String, Object> user = Map.of("maTK", 1, "vaiTro", "DELIVERY");

        when(request.getServletPath()).thenReturn("/login");
        when(accountDAO.emailExists(email)).thenReturn(true);
        when(accountDAO.login(email, PasswordUtil.hash(password))).thenReturn(user);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra phiên đăng nhập và trang đích
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/admin/order-tracking");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_048 - Khách quay về danh sách yêu thích")
    public void testTaiKhoan_customerWishlistRedirect_048() throws Exception {
        // 1. Chuẩn bị tài khoản
        String email = "qa@example.com";
        String password = "Abcd123!";
        Map<String, Object> user = Map.of("maTK", 1, "vaiTro", "CUSTOMER");

        when(request.getServletPath()).thenReturn("/login");
        when(accountDAO.emailExists(email)).thenReturn(true);
        when(accountDAO.login(email, PasswordUtil.hash(password))).thenReturn(user);
        when(request.getParameter("next")).thenReturn("wishlist");

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra phiên đăng nhập và trang đích
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/wishlist");
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_049 - Email chưa đăng ký")
    public void testTaiKhoan_unknownEmailCannotLogin_049() throws Exception {
        // 1. Chuẩn bị dữ liệu xác thực thất bại
        when(request.getServletPath()).thenReturn("/login");
        when(accountDAO.emailExists("qa@example.com")).thenReturn(false);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra lỗi và phiên đăng nhập
        assertEquals("login.jsp", controller.page, "Đăng nhập sai phải giữ form");
        verify(request).setAttribute("error", "Email này chưa có trong hệ thống.");
        verify(session, never()).setAttribute(eq("auth"), any());
        verify(response, never()).sendRedirect(anyString());
        verify(accountDAO, never()).login(any(), any());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_JU_050 - DAO không xác thực được tài khoản")
    public void testTaiKhoan_wrongPasswordOrLockedAccountCannotLogin_050() throws Exception {
        // 1. Chuẩn bị dữ liệu xác thực thất bại
        when(request.getServletPath()).thenReturn("/login");
        when(accountDAO.emailExists("qa@example.com")).thenReturn(true);
        when(accountDAO.login("qa@example.com", PasswordUtil.hash("Abcd123!"))).thenReturn(null);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra lỗi và phiên đăng nhập
        assertEquals("login.jsp", controller.page, "Đăng nhập sai phải giữ form");
        verify(request).setAttribute("error", "Mật khẩu không đúng hoặc tài khoản đang bị khóa.");
        verify(session, never()).setAttribute(eq("auth"), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_051 - Đăng xuất phiên hiện tại")
    public void testTaiKhoan_logoutInvalidatesSession_051() throws Exception {
        // 1. Chuẩn bị đường dẫn đăng xuất
        when(request.getServletPath()).thenReturn("/logout");

        // 2. Đăng xuất
        controller.doGet(request, response);

        // 3. Kiểm tra session và điều hướng
        verify(session).invalidate();
        verify(response).sendRedirect("/celine/home");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_LOGIN_001 - Đúng mật khẩu khách")
    public void testDangNhap_DuLieuCuThe_001() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "customer01@example.com";
        String password = "Khach123!";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("customer01@example.com")).thenReturn(true);
        Map<String, Object> user = Map.of("maTK", 101, "vaiTro", "CUSTOMER");
        when(accountDAO.login("customer01@example.com", PasswordUtil.hash(password))).thenReturn(user);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        verify(accountDAO).login("customer01@example.com", PasswordUtil.hash(password));
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/home");
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_LOGIN_002 - Sai mật khẩu một ký tự")
    public void testDangNhap_DuLieuCuThe_002() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "customer01@example.com";
        String password = "Khach123@";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("customer01@example.com")).thenReturn(true);
        when(accountDAO.login("customer01@example.com", PasswordUtil.hash(password))).thenReturn(null);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("login.jsp", controller.page, "Thông tin sai phải giữ trang đăng nhập");
        verify(request).setAttribute("error", "Mật khẩu không đúng hoặc tài khoản đang bị khóa.");
        verify(session, never()).setAttribute(eq("auth"), any());
        verify(response, never()).sendRedirect(anyString());
        verify(accountDAO).login("customer01@example.com", PasswordUtil.hash(password));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_LOGIN_003 - Sai chữ hoa/thường trong mật khẩu")
    public void testDangNhap_DuLieuCuThe_003() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "customer01@example.com";
        String password = "khach123!";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("customer01@example.com")).thenReturn(true);
        when(accountDAO.login("customer01@example.com", PasswordUtil.hash(password))).thenReturn(null);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("login.jsp", controller.page, "Thông tin sai phải giữ trang đăng nhập");
        verify(request).setAttribute("error", "Mật khẩu không đúng hoặc tài khoản đang bị khóa.");
        verify(session, never()).setAttribute(eq("auth"), any());
        verify(response, never()).sendRedirect(anyString());
        verify(accountDAO).login("customer01@example.com", PasswordUtil.hash(password));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_LOGIN_004 - Mật khẩu rỗng")
    public void testDangNhap_DuLieuCuThe_004() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "customer01@example.com";
        String password = "";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("customer01@example.com")).thenReturn(true);
        when(accountDAO.login("customer01@example.com", PasswordUtil.hash(password))).thenReturn(null);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("login.jsp", controller.page, "Thông tin sai phải giữ trang đăng nhập");
        verify(request).setAttribute("error", "Mật khẩu không đúng hoặc tài khoản đang bị khóa.");
        verify(session, never()).setAttribute(eq("auth"), any());
        verify(response, never()).sendRedirect(anyString());
        verify(accountDAO).login("customer01@example.com", PasswordUtil.hash(password));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_LOGIN_005 - Thiếu tham số mật khẩu")
    public void testDangNhap_DuLieuCuThe_005() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "customer01@example.com";
        String password = null;
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("customer01@example.com")).thenReturn(true);
        when(accountDAO.login("customer01@example.com", PasswordUtil.hash(password))).thenReturn(null);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("login.jsp", controller.page, "Thông tin sai phải giữ trang đăng nhập");
        verify(request).setAttribute("error", "Mật khẩu không đúng hoặc tài khoản đang bị khóa.");
        verify(session, never()).setAttribute(eq("auth"), any());
        verify(response, never()).sendRedirect(anyString());
        verify(accountDAO).login("customer01@example.com", PasswordUtil.hash(password));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_LOGIN_006 - Dấu cách cuối mật khẩu")
    public void testDangNhap_DuLieuCuThe_006() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "customer01@example.com";
        String password = "Khach123! ";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("customer01@example.com")).thenReturn(true);
        when(accountDAO.login("customer01@example.com", PasswordUtil.hash(password))).thenReturn(null);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("login.jsp", controller.page, "Thông tin sai phải giữ trang đăng nhập");
        verify(request).setAttribute("error", "Mật khẩu không đúng hoặc tài khoản đang bị khóa.");
        verify(session, never()).setAttribute(eq("auth"), any());
        verify(response, never()).sendRedirect(anyString());
        verify(accountDAO).login("customer01@example.com", PasswordUtil.hash(password));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_LOGIN_007 - Email chưa tồn tại")
    public void testDangNhap_DuLieuCuThe_007() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "unknown01@example.com";
        String password = "Khach123!";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("unknown01@example.com")).thenReturn(false);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("login.jsp", controller.page, "Thông tin sai phải giữ trang đăng nhập");
        verify(request).setAttribute("error", "Email này chưa có trong hệ thống.");
        verify(session, never()).setAttribute(eq("auth"), any());
        verify(response, never()).sendRedirect(anyString());
        verify(accountDAO, never()).login(any(), any());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC_LOGIN_008 - Email sai định dạng")
    public void testDangNhap_DuLieuCuThe_008() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "customer01example.com";
        String password = "Khach123!";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        assertEquals("login.jsp", controller.page, "Thông tin sai phải giữ trang đăng nhập");
        verify(request).setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
        verify(session, never()).setAttribute(eq("auth"), any());
        verify(response, never()).sendRedirect(anyString());
        verifyNoInteractions(accountDAO);
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_LOGIN_009 - Email trim và chữ thường")
    public void testDangNhap_DuLieuCuThe_009() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "  CUSTOMER01@EXAMPLE.COM  ";
        String password = "Khach123!";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("customer01@example.com")).thenReturn(true);
        Map<String, Object> user = Map.of("maTK", 101, "vaiTro", "CUSTOMER");
        when(accountDAO.login("customer01@example.com", PasswordUtil.hash(password))).thenReturn(user);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        verify(accountDAO).login("customer01@example.com", PasswordUtil.hash(password));
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/home");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_LOGIN_010 - Đúng tài khoản quản trị")
    public void testDangNhap_DuLieuCuThe_010() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "admin01@example.com";
        String password = "Admin123!";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("admin01@example.com")).thenReturn(true);
        Map<String, Object> user = Map.of("maTK", 102, "vaiTro", "ADMIN");
        when(accountDAO.login("admin01@example.com", PasswordUtil.hash(password))).thenReturn(user);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        verify(accountDAO).login("admin01@example.com", PasswordUtil.hash(password));
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/admin/dashboard");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_LOGIN_011 - Đúng tài khoản nhân viên")
    public void testDangNhap_DuLieuCuThe_011() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "staff01@example.com";
        String password = "Staff123!";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("staff01@example.com")).thenReturn(true);
        Map<String, Object> user = Map.of("maTK", 103, "vaiTro", "STAFF");
        when(accountDAO.login("staff01@example.com", PasswordUtil.hash(password))).thenReturn(user);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        verify(accountDAO).login("staff01@example.com", PasswordUtil.hash(password));
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/admin/orders");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_LOGIN_012 - Đúng tài khoản giao hàng")
    public void testDangNhap_DuLieuCuThe_012() throws Exception {
        // 1. Chuẩn bị email và mật khẩu thử
        String email = "delivery01@example.com";
        String password = "Ship123!";
        when(request.getServletPath()).thenReturn("/login");
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(accountDAO.emailExists("delivery01@example.com")).thenReturn(true);
        Map<String, Object> user = Map.of("maTK", 104, "vaiTro", "DELIVERY");
        when(accountDAO.login("delivery01@example.com", PasswordUtil.hash(password))).thenReturn(user);

        // 2. Đăng nhập
        controller.doPost(request, response);

        // 3. Kiểm tra kết quả
        verify(accountDAO).login("delivery01@example.com", PasswordUtil.hash(password));
        verify(session).setAttribute("auth", user);
        verify(response).sendRedirect("/celine/admin/order-tracking");
    }

}
