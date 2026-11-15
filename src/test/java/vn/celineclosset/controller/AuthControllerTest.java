package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.MockedConstruction;
import vn.celineclosset.dao.AccountDAO;
import vn.celineclosset.util.PasswordUtil;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Celine Closet: dang ky va dang nhap")
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class AuthControllerTest {
    // Chi thay viec render JSP; logic doPost/register/login van la code that.
    static class TestableAuth extends AuthController {
        String page;
        @Override protected void view(HttpServletRequest q, HttpServletResponse s, String jsp) { page = jsp; }
    }
    MockedConstruction<AccountDAO> scope;
    AccountDAO dao;
    TestableAuth controller;
    HttpServletRequest req;
    HttpServletResponse resp;
    HttpSession session;
    @BeforeEach void setUp() {
        scope = mockConstruction(AccountDAO.class);
        controller = new TestableAuth();
        dao = scope.constructed().get(0);
        req = mock(HttpServletRequest.class); resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/celine");
        when(req.getServletPath()).thenReturn("/register");
        when(req.getParameter("hoTen")).thenReturn("QA Khang");
        when(req.getParameter("email")).thenReturn("qa@example.com");
        when(req.getParameter("soDienThoai")).thenReturn("0912345678");
        password("Abcd123!");
    }
    @AfterEach void tearDown() { if (scope != null) scope.close(); }
    void password(String value) {
        when(req.getParameter("password")).thenReturn(value);
        when(req.getParameter("confirmPassword")).thenReturn(value);
    }
    void assertRegistration(boolean valid) throws Exception {
        controller.doPost(req, resp);
        if (valid) {
            String expectedHash = PasswordUtil.hash(req.getParameter("password"));
            verify(dao).register(eq("QA Khang"), eq("qa@example.com"),
                eq(expectedHash), eq("0912345678"));
            assertEquals("login.jsp", controller.page);
            verify(req).setAttribute(eq("success"), anyString());
            verify(req, never()).setAttribute(eq("error"), any());
        } else {
            verify(dao, never()).register(any(), any(), any(), any());
            verify(req).setAttribute(eq("error"), anyString());
            assertEquals("register.jsp", controller.page);
        }
    }
    @ParameterizedTest(name="Mat khau {0} ky tu: hop le={1}")
    @CsvSource({"7,false", "8,true", "9,true", "31,true", "32,true", "33,false"})   // test kết quả mong đợi false @CsvSource({"7,true", "8,true", "9,true", "31,true", "32,true", "33,false"}) 

    void passwordBoundaries(int length, boolean valid) throws Exception {
        password("Ab1!" + "a".repeat(length-4));
        assertRegistration(valid);
    }
    @ParameterizedTest(name="Mat khau [{0}], hop le={1}")
    @CsvSource({"abcdefgh1!,false", "ABCDEFGH1!,false", "Abcdefgh!,false", "Abcdefg12,false",
        "Abcd123?,false", "Abcd123!,true", "Abcd123@,true", "Abcd123#,true", "Abcd123$,true",
        "Abcd123%,true", "Abcd123^,true", "Abcd123&,true", "Abcd123*,true", "'Abcd 123!',false"})
    void passwordRules(String value, boolean valid) throws Exception { password(value); assertRegistration(valid); }
    @ParameterizedTest @NullAndEmptySource
    void missingPassword(String value) throws Exception { password(value); assertRegistration(false); }
    @ParameterizedTest(name="Dien thoai [{0}], hop le={1}")
    @CsvSource({"091234567,false", "0912345678,true", "09123456789,false", "1912345678,false",
        "091234567a,false", "'091 2345678',false", "' 0912345678 ',true"})
    void phoneRules(String value, boolean valid) throws Exception {
        when(req.getParameter("soDienThoai")).thenReturn(value); assertRegistration(valid);
    }
    @ParameterizedTest @NullAndEmptySource
    void missingPhone(String value) throws Exception {
        when(req.getParameter("soDienThoai")).thenReturn(value); assertRegistration(false);
    }
    @ParameterizedTest @ValueSource(strings={"abc", "@mail.com", "a@@mail.com", "a@b", "a@b.", "a b@mail.com"})
    void invalidEmail(String value) throws Exception {
        when(req.getParameter("email")).thenReturn(value); assertRegistration(false);
        verifyNoInteractions(dao);
    }
    @ParameterizedTest @NullAndEmptySource
    void missingEmail(String value) throws Exception {
        when(req.getParameter("email")).thenReturn(value); assertRegistration(false);
    }
    @Test void normalizeEmailBeforeSaving() throws Exception {
        when(req.getParameter("email")).thenReturn("  QA@EXAMPLE.COM  "); assertRegistration(true);
    }
    @Test void duplicateEmailIsRejected() throws Exception {
        when(dao.emailExists("qa@example.com")).thenReturn(true);
        assertRegistration(false); verify(req).setAttribute("error", "Email đã tồn tại.");
    }
    @Test void confirmationMismatchIsRejected() throws Exception {
        when(req.getParameter("confirmPassword")).thenReturn("Khac123!"); assertRegistration(false);
        verify(req).setAttribute("error", "Mật khẩu nhập lại không khớp.");
    }
    @Test void databaseErrorIsWrappedAsServletException() throws Exception {
        SQLException cause = new SQLException("Database unavailable");
        when(dao.emailExists(anyString())).thenThrow(cause);
        ServletException error = assertThrows(ServletException.class, () -> controller.doPost(req, resp));
        assertSame(cause, error.getCause());
        verify(dao, never()).register(any(), any(), any(), any());
    }
    @ParameterizedTest
    @CsvSource({"CUSTOMER,/home", "ADMIN,/admin/dashboard", "STAFF,/admin/orders", "DELIVERY,/admin/order-tracking"})
    void loginRedirectsByRole(String role, String path) throws Exception {
        when(req.getServletPath()).thenReturn("/login");
        when(dao.emailExists("qa@example.com")).thenReturn(true);
        Map<String,Object> user = Map.of("maTK",1,"vaiTro",role);
        when(dao.login("qa@example.com", PasswordUtil.hash("Abcd123!"))).thenReturn(user);
        controller.doPost(req,resp);
        verify(session).setAttribute("auth",user); verify(resp).sendRedirect("/celine"+path);
    }
    @Test void customerWishlistRedirect() throws Exception {
        when(req.getParameter("next")).thenReturn("wishlist");
        loginRedirectsByRole("CUSTOMER","/wishlist");
    }
    @Test void unknownEmailCannotLogin() throws Exception {
        when(req.getServletPath()).thenReturn("/login"); controller.doPost(req,resp);
        verify(dao,never()).login(any(),any());
        verify(session,never()).setAttribute(eq("auth"),any()); assertEquals("login.jsp",controller.page);
    }
    @Test void wrongPasswordOrLockedAccountCannotLogin() throws Exception {
        when(req.getServletPath()).thenReturn("/login"); when(dao.emailExists(any())).thenReturn(true);
        when(dao.login(any(),any())).thenReturn(null); controller.doPost(req,resp);
        verify(session,never()).setAttribute(eq("auth"),any());
        verify(req).setAttribute("error","Mật khẩu không đúng hoặc tài khoản đang bị khóa.");
        assertEquals("login.jsp",controller.page);
    }
    @Test void logoutInvalidatesSession() throws Exception {
        when(req.getServletPath()).thenReturn("/logout"); controller.doGet(req,resp);
        verify(session).invalidate(); verify(resp).sendRedirect("/celine/home");
    }
}
