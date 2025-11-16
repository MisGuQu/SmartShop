package com.smartshop.controller;

import com.smartshop.dto.product.CategoryForm;
import com.smartshop.dto.product.ProductForm;
import com.smartshop.entity.enums.OrderStatus;
import com.smartshop.entity.order.Order;
import com.smartshop.entity.product.Category;
import com.smartshop.entity.product.Product;
import com.smartshop.entity.user.Role;
import com.smartshop.entity.user.User;
import com.smartshop.service.CategoryService;
import com.smartshop.service.DashboardService;
import com.smartshop.service.OrderService;
import com.smartshop.service.ProductService;
import com.smartshop.service.ReportService;
import com.smartshop.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final ReportService reportService;

    // ============================================================
    // DASHBOARD & INDEX
    // ============================================================

    @GetMapping({"", "/", "/index"})
    public String adminDashboard() {
        return "admin/index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Lấy tất cả thống kê và đưa vào model
        model.addAttribute("totalRevenueLast7Days", dashboardService.getTotalRevenueLast7Days());
        model.addAttribute("revenueByDayLast7Days", dashboardService.getRevenueByDayLast7Days());
        model.addAttribute("newOrdersLast24Hours", dashboardService.getNewOrdersLast24Hours());
        model.addAttribute("top5BestSellingProducts", dashboardService.getTop5BestSellingProductsThisMonth());
        model.addAttribute("revenueByCategory", dashboardService.getRevenueByCategory());
        model.addAttribute("revenueByPaymentMethod", dashboardService.getRevenueByPaymentMethod());
        model.addAttribute("topCustomers", dashboardService.getTopCustomers());
        model.addAttribute("voucherEffectiveness", dashboardService.getVoucherEffectiveness());
        return "admin/dashboard";
    }

    @GetMapping("/dashboard/export")
    public ResponseEntity<byte[]> exportReport(@RequestParam String format) {
        try {
            byte[] reportData;
            String contentType;
            String fileName;

            if ("excel".equalsIgnoreCase(format)) {
                reportData = reportService.exportExcel();
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                fileName = "bao-cao-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            } else if ("pdf".equalsIgnoreCase(format)) {
                reportData = reportService.exportPDF();
                contentType = "application/pdf";
                fileName = "bao-cao-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            } else {
                return ResponseEntity.badRequest().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(reportData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================
    // PRODUCTS MANAGEMENT
    // ============================================================

    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/products")
    public String showProductManagement(@RequestParam(value = "editId", required = false) Long editId,
                                        Model model) {
        model.addAttribute("products", productService.getAllProducts());

        if (!model.containsAttribute("productForm")) {
            if (editId != null) {
                Product product = productService.getProductOrThrow(editId);
                model.addAttribute("productForm", mapToForm(product));
                model.addAttribute("editingProductId", editId);
            } else {
                model.addAttribute("productForm", new ProductForm());
            }
        } else if (editId != null) {
            model.addAttribute("editingProductId", editId);
        }

        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }

        return "admin/products";
    }

    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                @RequestParam(value = "images", required = false) MultipartFile[] images,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return handleProductValidationErrors(model, null);
        }

        try {
            Product product = productService.createProduct(mapToEntity(productForm), productForm.getCategoryId());
            // Upload hình ảnh nếu có
            if (images != null && images.length > 0) {
                productService.uploadProductImages(product.getId(), images);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công");
            return "redirect:/admin/products";
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            bindingResult.reject("product.error", safeMessage(ex.getMessage(), "Không thể lưu sản phẩm"));
        } catch (DataIntegrityViolationException ex) {
            String message = safeMessage(ex.getMessage(), "Không thể lưu sản phẩm");
            bindingResult.reject("product.error", "Không thể lưu sản phẩm: " + message);
        } catch (Exception ex) {
            bindingResult.reject("product.error", "Lỗi khi upload hình ảnh: " + ex.getMessage());
        }

        return handleProductValidationErrors(model, null);
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                @RequestParam(value = "images", required = false) MultipartFile[] images,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return handleProductValidationErrors(model, id);
        }

        try {
            productService.updateProduct(id, mapToEntity(productForm), productForm.getCategoryId());
            // Upload hình ảnh mới nếu có
            if (images != null && images.length > 0) {
                productService.uploadProductImages(id, images);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công");
            return "redirect:/admin/products";
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            bindingResult.reject("product.error", safeMessage(ex.getMessage(), "Không thể cập nhật sản phẩm"));
        } catch (DataIntegrityViolationException ex) {
            String message = safeMessage(ex.getMessage(), "Không thể cập nhật sản phẩm");
            bindingResult.reject("product.error", "Không thể cập nhật sản phẩm: " + message);
        } catch (Exception ex) {
            bindingResult.reject("product.error", "Lỗi khi upload hình ảnh: " + ex.getMessage());
        }

        return handleProductValidationErrors(model, id);
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm");
        } catch (EntityNotFoundException | DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", safeMessage(ex.getMessage(), "Không thể xóa sản phẩm"));
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/categories")
    public String createCategory(@Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            if (!model.containsAttribute("productForm")) {
                model.addAttribute("productForm", new ProductForm());
            }
            model.addAttribute("products", productService.getAllProducts());
            return "admin/products";
        }

        try {
            categoryService.createCategory(
                    categoryForm.getName(),
                    categoryForm.getParentId()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công");
            return "redirect:/admin/products";
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            bindingResult.reject("category.error", safeMessage(ex.getMessage(), "Không thể lưu danh mục"));
        } catch (DataIntegrityViolationException ex) {
            bindingResult.reject("category.error", safeMessage(ex.getMessage(), "Không thể lưu danh mục"));
        }

        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductForm());
        }
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }

    @PostMapping("/products/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", safeMessage(ex.getMessage(), "Danh mục không tồn tại"));
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", safeMessage(ex.getMessage(), "Không thể xóa danh mục đang chứa sản phẩm"));
        }
        return "redirect:/admin/products";
    }

    // ============================================================
    // ORDERS MANAGEMENT
    // ============================================================

    @GetMapping("/orders")
    public String showOrderManagement(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String keyword,
                                     Model model) {
        List<Order> orders;
        
        if ((status != null && !status.isEmpty()) || (keyword != null && !keyword.isEmpty())) {
            OrderStatus orderStatus = null;
            if (status != null && !status.isEmpty()) {
                try {
                    orderStatus = OrderStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    // Invalid status, ignore
                }
            }
            orders = orderService.searchAndFilterOrders(orderStatus, keyword);
        } else {
            orders = orderService.getAllOrders();
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String showOrderDetail(@PathVariable Long id, Model model) {
        try {
            Order order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            model.addAttribute("orderStatuses", OrderStatus.values());
            return "admin/order-detail";
        } catch (Exception ex) {
            return "redirect:/admin/orders?error=Order not found";
        }
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã cập nhật trạng thái đơn hàng " + order.getOrderNumber() + " thành " + status);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Không thể cập nhật trạng thái đơn hàng: " + ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    // ============================================================
    // USERS MANAGEMENT
    // ============================================================

    @GetMapping("/users")
    public String showUserManagement(Model model) {
        List<User> users = userService.getAllUsers();
        List<Role> roles = userService.getAllRoles();
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleUserActiveStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.toggleUserActiveStatus(id);
            String message = user.isActive() 
                ? "Đã kích hoạt tài khoản: " + user.getFullName()
                : "Đã vô hiệu hóa tài khoản: " + user.getFullName();
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thay đổi trạng thái tài khoản: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/roles/add")
    public String addRoleToUser(@PathVariable Long id, 
                                @RequestParam Long roleId,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.addRoleToUser(id, roleId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã thêm quyền truy cập cho: " + user.getFullName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Không thể thêm quyền truy cập: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/roles/remove")
    public String removeRoleFromUser(@PathVariable Long id,
                                     @RequestParam Long roleId,
                                     RedirectAttributes redirectAttributes) {
        try {
            User user = userService.removeRoleFromUser(id, roleId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã xóa quyền truy cập khỏi: " + user.getFullName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Không thể xóa quyền truy cập: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/roles/change")
    public String changeUserRole(@PathVariable Long id,
                                @RequestParam Long roleId,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.changeUserRole(id, roleId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã thay đổi vai trò cho: " + user.getFullName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Không thể thay đổi vai trò: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ============================================================
    // OTHER PAGES
    // ============================================================

    @GetMapping("/vouchers")
    public String manageVouchers() {
        return "admin/vouchers";
    }

    @GetMapping("/reviews")
    public String manageReviews() {
        return "admin/reviews";
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private String handleProductValidationErrors(Model model, Long editingProductId) {
        model.addAttribute("products", productService.getAllProducts());
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }
        if (editingProductId != null) {
            model.addAttribute("editingProductId", editingProductId);
        }
        return "admin/products";
    }

    private Product mapToEntity(ProductForm form) {
        Product product = new Product();
        product.setId(form.getId());
        product.setName(form.getName());
        product.setSlug(form.getSlug());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setHasVariants(form.isHasVariants());
        product.setActive(form.isActive());
        return product;
    }

    private ProductForm mapToForm(Product product) {
        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setName(product.getName());
        form.setSlug(product.getSlug());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setHasVariants(product.isHasVariants());
        form.setActive(product.isActive());
        if (product.getCategory() != null) {
            form.setCategoryId(product.getCategory().getId());
        }
        return form;
    }

    @NonNull
    private String safeMessage(@Nullable String candidate, @NonNull String fallback) {
        return candidate != null ? candidate : fallback;
    }
}
