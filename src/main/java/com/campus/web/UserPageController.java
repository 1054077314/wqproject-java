package com.campus.web;

import com.campus.appointment.service.AppointmentService;
import com.campus.category.service.CategoryService;
import com.campus.comment.service.CommentService;
import com.campus.favorite.service.FavoriteService;
import com.campus.product.dto.ProductCreateRequest;
import com.campus.product.service.ProductService;
import com.campus.security.SecurityUtils;
import com.campus.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class UserPageController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AppointmentService appointmentService;
    private final FavoriteService favoriteService;
    private final CommentService commentService;

    public UserPageController(ProductService productService, CategoryService categoryService,
                              AppointmentService appointmentService, FavoriteService favoriteService,
                              CommentService commentService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.appointmentService = appointmentService;
        this.favoriteService = favoriteService;
        this.commentService = commentService;
    }

    @GetMapping("/publish")
    public String publishForm(Model model) {
        model.addAttribute("categories", categoryService.listAll());
        model.addAttribute("user", SecurityUtils.currentUser().orElse(null));
        return "products/publish";
    }

    @PostMapping("/publish")
    public String publish(@RequestParam String title,
                          @RequestParam String description,
                          @RequestParam BigDecimal price,
                          @RequestParam Long category,
                          @RequestParam("contact_info") String contactInfo,
                          @RequestParam(value = "files", required = false) List<MultipartFile> files,
                          RedirectAttributes ra) {
        try {
            ProductCreateRequest req = new ProductCreateRequest();
            req.setTitle(title);
            req.setDescription(description);
            req.setPrice(price);
            req.setCategory(category);
            req.setContactInfo(contactInfo);
            Map<String, Object> created = productService.create(req, files, SecurityUtils.requireUser());
            ra.addFlashAttribute("message", "发布成功，等待审核");
            return "redirect:/products/" + created.get("id");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/publish";
        }
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request,
                          @RequestParam(value = "tab", defaultValue = "products") String tab,
                          Model model) {
        UserPrincipal user = SecurityUtils.requireUser();
        model.addAttribute("user", user);
        model.addAttribute("tab", tab);
        if ("products".equals(tab)) {
            model.addAttribute("myProducts", productService.myProducts(request, null, user).getResults());
        } else if ("buyer".equals(tab)) {
            model.addAttribute("appointments", appointmentService.asBuyer(request, user).getResults());
        } else if ("seller".equals(tab)) {
            model.addAttribute("appointments", appointmentService.asSeller(request, user).getResults());
        } else if ("favorites".equals(tab)) {
            model.addAttribute("favorites", favoriteService.myFavorites(request, user).getResults());
        }
        return "profile";
    }

    @PostMapping("/appointments/{id}/action")
    public String appointmentAction(@PathVariable Long id, @RequestParam String action, RedirectAttributes ra) {
        try {
            appointmentService.updateStatus(id, action, SecurityUtils.requireUser());
            ra.addFlashAttribute("message", "操作成功");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile?tab=seller";
    }

    @PostMapping("/products/{id}/appoint")
    public String appoint(@PathVariable Long id, RedirectAttributes ra) {
        try {
            appointmentService.create(id, SecurityUtils.requireUser());
            ra.addFlashAttribute("message", "预约成功");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + id;
    }

    @PostMapping("/products/{id}/favorite")
    public String favorite(@PathVariable Long id, RedirectAttributes ra) {
        try {
            favoriteService.toggle(id, SecurityUtils.requireUser());
            ra.addFlashAttribute("message", "已更新收藏");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + id;
    }

    @PostMapping("/products/{id}/comment")
    public String comment(@PathVariable Long id, @RequestParam String content, RedirectAttributes ra) {
        try {
            commentService.create(id, content, SecurityUtils.requireUser());
            ra.addFlashAttribute("message", "留言成功");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + id;
    }
}
