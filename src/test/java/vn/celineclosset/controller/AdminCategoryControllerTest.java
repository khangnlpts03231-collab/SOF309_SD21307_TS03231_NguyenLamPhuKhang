package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.MockedConstruction;
import vn.celineclosset.dao.*;
import java.sql.SQLException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Timeout(10)
@DisplayName("Celine Closet: CRUD danh muc va phan quyen")
class AdminCategoryControllerTest {
    static class TestableCategory extends AdminCategoryController {
        String page;
        @Override protected void view(HttpServletRequest q, HttpServletResponse s,String jsp) { page=jsp; }
    }
    MockedConstruction<AccountDAO> accounts;
    MockedConstruction<CategoryDAO> categories;
    TestableCategory controller; AccountDAO account; CategoryDAO category;
    HttpServletRequest req; HttpServletResponse resp; HttpSession session;
    @BeforeEach void setUp() throws Exception {
        accounts=mockConstruction(AccountDAO.class); categories=mockConstruction(CategoryDAO.class);
        controller=new TestableCategory(); account=accounts.constructed().get(0);
        // BaseController va AdminCategoryController deu khoi tao CategoryDAO; lay DAO cua lop con.
        category=categories.constructed().get(categories.constructed().size()-1);
        req=mock(HttpServletRequest.class); resp=mock(HttpServletResponse.class); session=mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(session); when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/celine"); role("ADMIN");
    }
    void role(String value) throws Exception {
        Map<String,Object> user=Map.of("maTK",1,"vaiTro",value);
        when(session.getAttribute("auth")).thenReturn(user); when(account.accountById(1)).thenReturn(user);
    }
    @AfterEach void tearDown(){
        if (categories != null) categories.close();
        if (accounts != null) accounts.close();
    }
    @ParameterizedTest @CsvSource({"hide,0", "show,1"})
    void changeStatus(String action,int status) throws Exception {
        when(req.getParameter("action")).thenReturn(action); when(req.getParameter("id")).thenReturn("7");
        controller.doPost(req,resp); verify(category).setCategoryStatus(7,status);
        verify(resp).sendRedirect("/celine/admin/categories");
    }
    @Test void deleteDelegatesToDao() throws Exception {
        when(req.getParameter("action")).thenReturn("delete"); when(req.getParameter("id")).thenReturn("7");
        controller.doPost(req,resp); verify(category).deleteCategory(7);
        verify(resp).sendRedirect("/celine/admin/categories");
    }
    @ParameterizedTest @ValueSource(strings={"", "7"})
    void addOrUpdateDelegatesExactForm(String id) throws Exception {
        Map<String,String[]> form=Map.of("maDM",new String[]{id},"tenDM",new String[]{"QA-Khang"},
            "moTa",new String[]{"Test"},"trangThai",new String[]{"1"});
        when(req.getParameterMap()).thenReturn(form); controller.doPost(req,resp);
        verify(category).saveCategory(same(form)); verify(resp).sendRedirect("/celine/admin/categories");
    }
    @Test void listCategories() throws Exception {
        List<Map<String,Object>> data=List.of(Map.of("maDM",7,"tenDM","QA"));
        when(category.categories(false)).thenReturn(data); controller.doGet(req,resp);
        verify(req).setAttribute("categories",data); assertEquals("admin/categories.jsp",controller.page);
    }
    @Test void loadEditCategory() throws Exception {
        when(req.getParameter("edit")).thenReturn("7"); Map<String,Object> data=Map.of("maDM",7,"tenDM","QA");
        when(category.category(7)).thenReturn(data); controller.doGet(req,resp);
        verify(req).setAttribute("editCategory",data);
    }
    @ParameterizedTest @CsvSource({"CUSTOMER,/home", "STAFF,/admin/orders", "DELIVERY,/admin/order-tracking"})
    void nonAdminCannotReadOrWrite(String role,String target) throws Exception {
        role(role); controller.doGet(req,resp); controller.doPost(req,resp);
        verifyNoInteractions(category); verify(resp,times(2)).sendRedirect("/celine"+target);
    }
    @Test void anonymousCannotReadOrWrite() throws Exception {
        when(req.getSession(false)).thenReturn(null); controller.doGet(req,resp); controller.doPost(req,resp);
        verifyNoInteractions(category); verify(resp,times(2)).sendRedirect("/celine/login?session=expired");
    }
    @ParameterizedTest @ValueSource(strings={"abc", "", "1.5", "999999999999999"})
    void malformedIdThrowsServletException(String id) throws Exception {
        when(req.getParameter("action")).thenReturn("delete"); when(req.getParameter("id")).thenReturn(id);
        ServletException ex=assertThrows(ServletException.class,()->controller.doPost(req,resp));
        assertInstanceOf(NumberFormatException.class,ex.getCause()); verifyNoInteractions(category);
    }
    @Test void databaseFailureIsWrapped() throws Exception {
        SQLException cause=new SQLException("Database unavailable"); when(category.categories(false)).thenThrow(cause);
        ServletException ex=assertThrows(ServletException.class,()->controller.doGet(req,resp));
        assertSame(cause,ex.getCause());
    }
}
