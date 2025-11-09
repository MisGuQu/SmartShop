package com.smartshop.controller;

import com.smartshop.entity.enums.OrderStatus;
import com.smartshop.entity.order.Order;
import com.smartshop.service.OrderService;
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
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
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

    @GetMapping("/{id}")
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

    @PostMapping("/{id}/status")
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
}

