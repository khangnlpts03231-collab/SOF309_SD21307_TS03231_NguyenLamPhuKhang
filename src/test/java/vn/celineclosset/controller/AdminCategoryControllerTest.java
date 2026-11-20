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

import vn.celineclosset.dao.CategoryDAO;
import java.util.List;

@DisplayName("Celine Closet - Danh mục và phân quyền")
@Timeout(10)
public class AdminCategoryControllerTest {

    private CategoryControllerForTest controller;
    private AccountDAO accountDAO;
    private CategoryDAO categoryDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private MockedConstruction<AccountDAO> accountScope;
    private MockedConstruction<CategoryDAO> categoryScope;

    private static class CategoryControllerForTest extends AdminCategoryController {
        private String page;

        @Override
        protected void view(HttpServletRequest request,
                            HttpServletResponse response, String jsp) {
            page = jsp;
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        accountScope = mockConstruction(AccountDAO.class);
        categoryScope = mockConstruction(CategoryDAO.class);
        controller = new CategoryControllerForTest();
        accountDAO = accountScope.constructed().get(0);
        // Controller con có CategoryDAO riêng, được tạo sau DAO của BaseController.
        int lastIndex = categoryScope.constructed().size() - 1;
        categoryDAO = categoryScope.constructed().get(lastIndex);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/celine");

        Map<String, Object> admin = Map.of("maTK", 1, "vaiTro", "ADMIN");
        when(session.getAttribute("auth")).thenReturn(admin);
        when(accountDAO.accountById(1)).thenReturn(admin);
    }

    @AfterEach
    public void tearDown() {
        if (categoryScope != null) {
            categoryScope.close();
        }
        if (accountScope != null) {
            accountScope.close();
        }
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_052 - Ẩn danh mục")
    public void testDanhMuc_changeStatus_052() throws Exception {
        // 1. Chuẩn bị mã và hành động
        when(request.getParameter("action")).thenReturn("hide");
        when(request.getParameter("id")).thenReturn("7");

        // 2. Gọi Controller
        controller.doPost(request, response);

        // 3. Kiểm tra trạng thái truyền DAO
        verify(categoryDAO).setCategoryStatus(7, 0);
        verify(response).sendRedirect("/celine/admin/categories");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_053 - Hiện danh mục")
    public void testDanhMuc_changeStatus_053() throws Exception {
        // 1. Chuẩn bị mã và hành động
        when(request.getParameter("action")).thenReturn("show");
        when(request.getParameter("id")).thenReturn("7");

        // 2. Gọi Controller
        controller.doPost(request, response);

        // 3. Kiểm tra trạng thái truyền DAO
        verify(categoryDAO).setCategoryStatus(7, 1);
        verify(response).sendRedirect("/celine/admin/categories");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_054 - Gửi lệnh xóa danh mục")
    public void testDanhMuc_deleteDelegatesToDao_054() throws Exception {
        // 1. Chuẩn bị danh mục cần xóa
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn("7");

        // 2. Xóa danh mục
        controller.doPost(request, response);

        // 3. Kiểm tra lời gọi DAO
        verify(categoryDAO).deleteCategory(7);
        verify(response).sendRedirect("/celine/admin/categories");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_055 - Thêm danh mục mới")
    public void testDanhMuc_addOrUpdateDelegatesExactForm_055() throws Exception {
        // 1. Chuẩn bị form danh mục
        Map<String, String[]> form = Map.of(
                "maDM", new String[]{""},
                "tenDM", new String[]{"QA-Khang"},
                "moTa", new String[]{"Test"},
                "trangThai", new String[]{"1"}
        );
        when(request.getParameterMap()).thenReturn(form);

        // 2. Lưu form
        controller.doPost(request, response);

        // 3. Kiểm tra form chuyển tới DAO
        verify(categoryDAO).saveCategory(same(form));
        verify(response).sendRedirect("/celine/admin/categories");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_056 - Lưu sửa danh mục mã 7")
    public void testDanhMuc_addOrUpdateDelegatesExactForm_056() throws Exception {
        // 1. Chuẩn bị form danh mục
        Map<String, String[]> form = Map.of(
                "maDM", new String[]{"7"},
                "tenDM", new String[]{"QA-Khang"},
                "moTa", new String[]{"Test"},
                "trangThai", new String[]{"1"}
        );
        when(request.getParameterMap()).thenReturn(form);

        // 2. Lưu form
        controller.doPost(request, response);

        // 3. Kiểm tra form chuyển tới DAO
        verify(categoryDAO).saveCategory(same(form));
        verify(response).sendRedirect("/celine/admin/categories");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_057 - Đọc danh sách danh mục")
    public void testDanhMuc_listCategories_057() throws Exception {
        // 1. Chuẩn bị danh sách thử
        List<Map<String, Object>> categories = List.of(Map.of("maDM", 7, "tenDM", "QA"));
        when(categoryDAO.categories(false)).thenReturn(categories);

        // 2. Xem danh sách
        controller.doGet(request, response);

        // 3. Kiểm tra dữ liệu và trang hiển thị
        verify(request).setAttribute("categories", categories);
        assertEquals("admin/categories.jsp", controller.page, "Phải mở trang danh mục");
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_JU_058 - Nạp danh mục vào model sửa")
    public void testDanhMuc_loadEditCategory_058() throws Exception {
        // 1. Chuẩn bị danh mục mã 7
        Map<String, Object> category = Map.of("maDM", 7, "tenDM", "QA");
        when(request.getParameter("edit")).thenReturn("7");
        when(categoryDAO.category(7)).thenReturn(category);

        // 2. Nạp form sửa
        controller.doGet(request, response);

        // 3. Kiểm tra bản ghi được chọn
        verify(request).setAttribute("editCategory", category);
        assertEquals("admin/categories.jsp", controller.page, "Phải mở trang danh mục");
    }

    @Test
    @Tag("permission")
    @DisplayName("TC_JU_059 - Chặn quyền CUSTOMER")
    public void testDanhMuc_nonAdminCannotReadOrWrite_059() throws Exception {
        // 1. Chuẩn bị phiên đăng nhập
        Map<String, Object> user = Map.of("maTK", 1, "vaiTro", "CUSTOMER");
        when(session.getAttribute("auth")).thenReturn(user);
        when(accountDAO.accountById(1)).thenReturn(user);

        // 2. Thử đọc và ghi danh mục
        controller.doGet(request, response);
        controller.doPost(request, response);

        // 3. Kiểm tra quyền truy cập
        verifyNoInteractions(categoryDAO);
        verify(response, times(2)).sendRedirect("/celine/home");
    }

    @Test
    @Tag("permission")
    @DisplayName("TC_JU_060 - Chặn quyền STAFF")
    public void testDanhMuc_nonAdminCannotReadOrWrite_060() throws Exception {
        // 1. Chuẩn bị phiên đăng nhập
        Map<String, Object> user = Map.of("maTK", 1, "vaiTro", "STAFF");
        when(session.getAttribute("auth")).thenReturn(user);
        when(accountDAO.accountById(1)).thenReturn(user);

        // 2. Thử đọc và ghi danh mục
        controller.doGet(request, response);
        controller.doPost(request, response);

        // 3. Kiểm tra quyền truy cập
        verifyNoInteractions(categoryDAO);
        verify(response, times(2)).sendRedirect("/celine/admin/orders");
    }

    @Test
    @Tag("permission")
    @DisplayName("TC_JU_061 - Chặn quyền DELIVERY")
    public void testDanhMuc_nonAdminCannotReadOrWrite_061() throws Exception {
        // 1. Chuẩn bị phiên đăng nhập
        Map<String, Object> user = Map.of("maTK", 1, "vaiTro", "DELIVERY");
        when(session.getAttribute("auth")).thenReturn(user);
        when(accountDAO.accountById(1)).thenReturn(user);

        // 2. Thử đọc và ghi danh mục
        controller.doGet(request, response);
        controller.doPost(request, response);

        // 3. Kiểm tra quyền truy cập
        verifyNoInteractions(categoryDAO);
        verify(response, times(2)).sendRedirect("/celine/admin/order-tracking");
    }

    @Test
    @Tag("permission")
    @DisplayName("TC_JU_062 - Chặn yêu cầu chưa có session")
    public void testDanhMuc_anonymousCannotReadOrWrite_062() throws Exception {
        // 1. Chuẩn bị phiên đăng nhập
        when(request.getSession(false)).thenReturn(null);

        // 2. Thử đọc và ghi danh mục
        controller.doGet(request, response);
        controller.doPost(request, response);

        // 3. Kiểm tra quyền truy cập
        verifyNoInteractions(categoryDAO);
        verify(response, times(2)).sendRedirect("/celine/login?session=expired");
    }

    @Test
    @Tag("exception")
    @DisplayName("TC_JU_063 - Mã danh mục không parse được 'abc'")
    public void testDanhMuc_malformedIdThrowsServletException_063() throws Exception {
        // 1. Chuẩn bị ID sai định dạng
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn("abc");

        // 2. Gọi Controller và bắt ngoại lệ
        ServletException error = assertThrows(ServletException.class,
                () -> controller.doPost(request, response));

        // 3. Kiểm tra nguyên nhân và DAO
        assertInstanceOf(NumberFormatException.class, error.getCause(), "Nguyên nhân phải là ID sai");
        verifyNoInteractions(categoryDAO);
    }

    @Test
    @Tag("exception")
    @DisplayName("TC_JU_064 - Mã danh mục không parse được ''")
    public void testDanhMuc_malformedIdThrowsServletException_064() throws Exception {
        // 1. Chuẩn bị ID sai định dạng
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn("");

        // 2. Gọi Controller và bắt ngoại lệ
        ServletException error = assertThrows(ServletException.class,
                () -> controller.doPost(request, response));

        // 3. Kiểm tra nguyên nhân và DAO
        assertInstanceOf(NumberFormatException.class, error.getCause(), "Nguyên nhân phải là ID sai");
        verifyNoInteractions(categoryDAO);
    }

    @Test
    @Tag("exception")
    @DisplayName("TC_JU_065 - Mã danh mục không parse được '1.5'")
    public void testDanhMuc_malformedIdThrowsServletException_065() throws Exception {
        // 1. Chuẩn bị ID sai định dạng
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn("1.5");

        // 2. Gọi Controller và bắt ngoại lệ
        ServletException error = assertThrows(ServletException.class,
                () -> controller.doPost(request, response));

        // 3. Kiểm tra nguyên nhân và DAO
        assertInstanceOf(NumberFormatException.class, error.getCause(), "Nguyên nhân phải là ID sai");
        verifyNoInteractions(categoryDAO);
    }

    @Test
    @Tag("exception")
    @DisplayName("TC_JU_066 - Mã danh mục không parse được '999999999999999'")
    public void testDanhMuc_malformedIdThrowsServletException_066() throws Exception {
        // 1. Chuẩn bị ID sai định dạng
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn("999999999999999");

        // 2. Gọi Controller và bắt ngoại lệ
        ServletException error = assertThrows(ServletException.class,
                () -> controller.doPost(request, response));

        // 3. Kiểm tra nguyên nhân và DAO
        assertInstanceOf(NumberFormatException.class, error.getCause(), "Nguyên nhân phải là ID sai");
        verifyNoInteractions(categoryDAO);
    }

    @Test
    @Tag("exception")
    @DisplayName("TC_JU_067 - Lỗi DAO khi tải danh sách")
    public void testDanhMuc_databaseFailureIsWrapped_067() throws Exception {
        // 1. Chuẩn bị lỗi đọc dữ liệu
        SQLException databaseError = new SQLException("Database unavailable");
        when(categoryDAO.categories(false)).thenThrow(databaseError);

        // 2. Xem danh sách và bắt ngoại lệ
        ServletException error = assertThrows(ServletException.class,
                () -> controller.doGet(request, response));

        // 3. Kiểm tra nguyên nhân
        assertSame(databaseError, error.getCause(), "Phải giữ nguyên lỗi SQL gốc");
    }

}
