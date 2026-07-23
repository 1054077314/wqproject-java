package com.campus.web;

import com.campus.category.dto.CategoryRequest;
import com.campus.category.service.CategoryService;
import com.campus.product.dto.ProductReviewRequest;
import com.campus.product.service.ProductService;
import com.campus.security.SecurityUtils;
import com.campus.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPageController {

    private final AuthService authService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminPageController(AuthService authService, ProductService productService, CategoryService categoryService) {
        this.authService = authService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping({"", "/"})
    public String admin(@RequestParam(value = "tab", defaultValue = "stats") String tab,
                        HttpServletRequest request,
                        Model model) {
        model.addAttribute("user", SecurityUtils.currentUser().orElse(null));
        model.addAttribute("tab", tab);
        if ("stats".equals(tab)) {
            model.addAttribute("stats", authService.statistics());
        } else if ("users".equals(tab)) {
            model.addAttribute("users", authService.listUsers(request).getResults());
        } else if ("review".equals(tab)) {
            model.addAttribute("pending", productService.pendingList(request).getResults());
        } else if ("categories".equals(tab)) {
            model.addAttribute("categories", categoryService.listAll());
        }
        return "admin/index";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, @RequestParam boolean isActive, RedirectAttributes ra) {
        try {
            authService.toggleActive(id, isActive, SecurityUtils.requireUser());
            ra.addFlashAttribute("message", "操作成功");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin?tab=users";
    }

    @PostMapping("/products/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam String action,
                         @RequestParam(required = false) String rejectReason,
                         RedirectAttributes ra) {
        try {
            ProductReviewRequest req = new ProductReviewRequest();
            req.setAction(action);
            req.setRejectReason(rejectReason);
            productService.review(id, req);
            ra.addFlashAttribute("message", "approve".equals(action) ? "审核通过" : "已驳回");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin?tab=review";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name,
                                 @RequestParam(defaultValue = "0") Integer sortOrder,
                                 RedirectAttributes ra) {
        try {
            CategoryRequest req = new CategoryRequest();
            req.setName(name);
            req.setSortOrder(sortOrder);
            categoryService.create(req);
            ra.addFlashAttribute("message", "创建成功");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("message", "删除成功");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin?tab=categories";
    }
}
