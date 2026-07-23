package com.campus.web;

import com.campus.category.service.CategoryService;
import com.campus.common.PageResult;
import com.campus.product.service.ProductService;
import com.campus.security.SecurityUtils;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.RegisterRequest;
import com.campus.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class PageController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AuthService authService;

    public PageController(ProductService productService, CategoryService categoryService, AuthService authService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @GetMapping({"/", "/home"})
    public String home(HttpServletRequest request, Model model) {
        PageResult<Map<String, Object>> page = productService.listActive(request, null, null);
        model.addAttribute("products", page.getResults().stream().limit(8).toList());
        model.addAttribute("categories", categoryService.listSimple());
        model.addAttribute("user", SecurityUtils.currentUser().orElse(null));
        return "home";
    }

    @GetMapping("/products")
    public String products(HttpServletRequest request,
                           @RequestParam(value = "category_id", required = false) Long categoryId,
                           @RequestParam(value = "search", required = false) String search,
                           Model model) {
        PageResult<Map<String, Object>> page = productService.listActive(request, categoryId, search);
        model.addAttribute("page", page);
        model.addAttribute("categories", categoryService.listSimple());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("search", search);
        model.addAttribute("user", SecurityUtils.currentUser().orElse(null));
        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.detail(id, SecurityUtils.currentUser().orElse(null)));
        model.addAttribute("user", SecurityUtils.currentUser().orElse(null));
        return "products/detail";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("mode", "login");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("mode", "register");
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletResponse response,
                          RedirectAttributes ra) {
        try {
            LoginRequest req = new LoginRequest();
            req.setUsername(username);
            req.setPassword(password);
            Map<String, Object> data = authService.login(req);
            Cookie cookie = new Cookie("token", String.valueOf(data.get("token")));
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(7 * 24 * 3600);
            response.addCookie(cookie);
            return "redirect:/";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             RedirectAttributes ra) {
        try {
            RegisterRequest req = new RegisterRequest();
            req.setUsername(username);
            req.setPassword(password);
            authService.register(req);
            ra.addFlashAttribute("message", "注册成功，请登录");
            return "redirect:/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        SecurityUtils.currentUser().ifPresent(authService::logout);
        Cookie cookie = new Cookie("token", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}
