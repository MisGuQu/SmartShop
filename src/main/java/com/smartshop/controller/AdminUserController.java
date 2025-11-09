package com.smartshop.controller;

import com.smartshop.entity.user.Role;
import com.smartshop.entity.user.User;
import com.smartshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public String showUserManagement(Model model) {
        List<User> users = userService.getAllUsers();
        List<Role> roles = userService.getAllRoles();
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        return "admin/users";
    }

    @PostMapping("/{id}/toggle-active")
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

    @PostMapping("/{id}/roles/add")
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

    @PostMapping("/{id}/roles/remove")
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

    @PostMapping("/{id}/roles/change")
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
}

