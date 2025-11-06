package com.smartshop.controller;

import com.smartshop.dto.auth.ForgotPasswordRequest;
import com.smartshop.dto.auth.LoginRequest;
import com.smartshop.dto.auth.RegisterRequest;
import com.smartshop.dto.auth.ResetPasswordRequest;
import com.smartshop.exception.AccountLockedException;
import com.smartshop.exception.AuthException;
import com.smartshop.security.JwtCookieService;
import com.smartshop.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "locked", required = false) String locked) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        model.addAttribute("errorMessage", error);
        model.addAttribute("lockedMessage", locked);
        return "auth/login";
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
                              BindingResult bindingResult,
                              HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("loginRequest", loginRequest);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.loginRequest", bindingResult);
            return "redirect:/auth/login";
        }

        try {
            var authResult = authService.login(loginRequest);
            ResponseCookie cookie = jwtCookieService.buildAccessTokenCookie(authResult.getToken());
            response.addHeader("Set-Cookie", cookie.toString());
            return "redirect:/";
        } catch (AccountLockedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản tạm khóa tới " + ex.getUnlockAt());
            return "redirect:/auth/login";
        } catch (AuthException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("registerRequest", request);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerRequest", bindingResult);
            return "redirect:/auth/register";
        }

        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công. Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (AuthException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("registerRequest", request);
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        if (!model.containsAttribute("forgotPasswordRequest")) {
            model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.forgotPasswordRequest", bindingResult);
            redirectAttributes.addFlashAttribute("forgotPasswordRequest", request);
            return "redirect:/auth/forgot-password";
    }

        try {
            authService.initiatePasswordReset(request);
            redirectAttributes.addFlashAttribute("successMessage", "Nếu email tồn tại, chúng tôi đã gửi liên kết đặt lại mật khẩu.");
        } catch (AuthException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setToken(token);
        if (!model.containsAttribute("resetPasswordRequest")) {
            model.addAttribute("resetPasswordRequest", resetPasswordRequest);
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.resetPasswordRequest", bindingResult);
            redirectAttributes.addFlashAttribute("resetPasswordRequest", request);
            return "redirect:/auth/reset-password?token=" + request.getToken();
        }

        try {
            authService.resetPassword(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (AuthException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/auth/reset-password?token=" + request.getToken();
        }
    }
}
