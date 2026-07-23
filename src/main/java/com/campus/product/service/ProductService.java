package com.campus.product.service;

import com.campus.appointment.mapper.AppointmentMapper;
import com.campus.category.mapper.CategoryMapper;
import com.campus.comment.mapper.CommentMapper;
import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.PageUtils;
import com.campus.config.AppProperties;
import com.campus.favorite.mapper.FavoriteMapper;
import com.campus.product.dto.ProductCreateRequest;
import com.campus.product.dto.ProductReviewRequest;
import com.campus.product.dto.ProductUpdateRequest;
import com.campus.product.entity.Product;
import com.campus.product.entity.ProductImage;
import com.campus.product.mapper.ProductImageMapper;
import com.campus.product.mapper.ProductMapper;
import com.campus.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final AppointmentMapper appointmentMapper;
    private final FavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final FileStorageService fileStorageService;
    private final AppProperties appProperties;

    public ProductService(ProductMapper productMapper, ProductImageMapper productImageMapper,
                          CategoryMapper categoryMapper, AppointmentMapper appointmentMapper,
                          FavoriteMapper favoriteMapper, CommentMapper commentMapper,
                          FileStorageService fileStorageService, AppProperties appProperties) {
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.categoryMapper = categoryMapper;
        this.appointmentMapper = appointmentMapper;
        this.favoriteMapper = favoriteMapper;
        this.commentMapper = commentMapper;
        this.fileStorageService = fileStorageService;
        this.appProperties = appProperties;
    }

    public PageResult<Map<String, Object>> listActive(HttpServletRequest request, Long categoryId, String search) {
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                productMapper.findActive(categoryId, search).stream().map(this::toListItem).collect(Collectors.toList()));
    }

    public Map<String, Object> detail(Long id, UserPrincipal principal) {
        Product product = requireProduct(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", product.getId());
        data.put("title", product.getTitle());
        data.put("description", product.getDescription());
        data.put("price", product.getPrice());
        data.put("images", productImageMapper.findByProductId(id).stream().map(this::toImageView).collect(Collectors.toList()));
        data.put("category_name", product.getCategoryName());
        data.put("contact_info", product.getContactInfo());
        data.put("status", product.getStatus());
        data.put("created_at", product.getCreatedAt());
        data.put("seller_username", product.getSellerUsername());
        data.put("comments", commentMapper.findByProductId(id).stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("content", c.getContent());
            m.put("username", c.getUsername());
            m.put("created_at", c.getCreatedAt());
            return m;
        }).collect(Collectors.toList()));
        data.put("appointment_count", appointmentMapper.countByProduct(id));
        boolean favorited = false;
        boolean appointed = false;
        if (principal != null) {
            favorited = favoriteMapper.exists(principal.getId(), id);
            appointed = appointmentMapper.exists(principal.getId(), id);
        }
        data.put("is_favorited", favorited);
        data.put("is_appointed", appointed);
        data.put("seller_id", product.getSellerId());
        return data;
    }

    @Transactional
    public Map<String, Object> create(ProductCreateRequest request, List<MultipartFile> files, UserPrincipal principal) {
        if (categoryMapper.findById(request.getCategory()) == null) {
            throw new BusinessException(400, "分类不存在");
        }
        Product product = new Product();
        product.setTitle(request.getTitle().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setCategoryId(request.getCategory());
        product.setSellerId(principal.getId());
        product.setContactInfo(request.getContactInfo().trim());
        product.setStatus("pending");
        product.setRejectReason("");
        product.setDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        saveImages(product.getId(), files, request.getUploadedImages());
        return toProductPayload(product.getId());
    }

    @Transactional
    public Map<String, Object> update(Long id, ProductUpdateRequest request, List<MultipartFile> files, UserPrincipal principal) {
        Product product = requireProduct(id);
        if (!product.getSellerId().equals(principal.getId())) {
            throw new BusinessException(403, "无权操作此商品");
        }
        if ("active".equals(product.getStatus())) {
            throw new BusinessException(400, "已上架商品需先下架再编辑");
        }
        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new BusinessException(400, "标题不能为空");
            }
            product.setTitle(title);
        }
        if (request.getDescription() != null) {
            String desc = request.getDescription().trim();
            if (desc.isEmpty()) {
                throw new BusinessException(400, "描述不能为空");
            }
            product.setDescription(desc);
        }
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "价格必须为正数");
            }
            product.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            if (categoryMapper.findById(request.getCategory()) == null) {
                throw new BusinessException(400, "分类不存在");
            }
            product.setCategoryId(request.getCategory());
        }
        if (request.getContactInfo() != null) {
            product.setContactInfo(request.getContactInfo().trim());
        }
        product.setStatus("pending");
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.update(product);

        Set<Long> keepIds = new HashSet<>();
        if (request.getKeepImageIds() != null && !request.getKeepImageIds().isBlank()) {
            for (String part : request.getKeepImageIds().split(",")) {
                if (!part.isBlank()) {
                    keepIds.add(Long.parseLong(part.trim()));
                }
            }
        }
        for (ProductImage img : productImageMapper.findByProductId(id)) {
            if (!keepIds.contains(img.getId())) {
                productImageMapper.softDelete(img.getId());
            }
        }
        saveImages(id, files, request.getUploadedImages());
        return toProductPayload(id);
    }

    @Transactional
    public void offline(Long id, UserPrincipal principal) {
        Product product = requireProduct(id);
        if (!product.getSellerId().equals(principal.getId())) {
            throw new BusinessException(403, "无权操作此商品");
        }
        if (appointmentMapper.countByProduct(id) > 0) {
            throw new BusinessException(400, "需先取消所有预约");
        }
        productMapper.updateStatus(id, "offline", product.getRejectReason());
    }

    public PageResult<Map<String, Object>> myProducts(HttpServletRequest request, String status, UserPrincipal principal) {
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                productMapper.findBySeller(principal.getId(), status).stream().map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("title", p.getTitle());
                    m.put("price", p.getPrice());
                    m.put("status", p.getStatus());
                    m.put("images", productImageMapper.findByProductId(p.getId()).stream().map(this::toImageView).collect(Collectors.toList()));
                    m.put("category_name", p.getCategoryName());
                    m.put("appointment_count", p.getAppointmentCount() == null ? 0 : p.getAppointmentCount());
                    m.put("created_at", p.getCreatedAt());
                    return m;
                }).collect(Collectors.toList()));
    }

    @Transactional
    public void review(Long id, ProductReviewRequest request) {
        Product product = requireProduct(id);
        if (!"pending".equals(product.getStatus())) {
            throw new BusinessException(400, "只能审核待审核商品");
        }
        if ("approve".equals(request.getAction())) {
            productMapper.updateStatus(id, "active", "");
            return;
        }
        if ("reject".equals(request.getAction())) {
            String reason = request.getRejectReason() == null ? "" : request.getRejectReason().trim();
            if (reason.isEmpty()) {
                throw new BusinessException(400, "驳回原因不能为空");
            }
            productMapper.updateStatus(id, "rejected", reason);
            return;
        }
        throw new BusinessException(400, "无效操作");
    }

    public PageResult<Map<String, Object>> pendingList(HttpServletRequest request) {
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                productMapper.findPending().stream().map(this::toListItem).collect(Collectors.toList()));
    }

    private void saveImages(Long productId, List<MultipartFile> files, List<String> base64List) {
        int count = 0;
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    if (++count > 3) {
                        throw new BusinessException(400, "最多上传 3 张图片");
                    }
                    insertImage(productId, fileStorageService.store(file));
                }
            }
        }
        if (base64List != null) {
            for (String data : base64List) {
                if (data != null && !data.isBlank()) {
                    if (++count > 3) {
                        throw new BusinessException(400, "最多上传 3 张图片");
                    }
                    insertImage(productId, fileStorageService.storeBase64(data));
                }
            }
        }
    }

    private void insertImage(Long productId, String path) {
        ProductImage image = new ProductImage();
        image.setProductId(productId);
        image.setImage(path);
        image.setDeleted(false);
        image.setCreatedAt(LocalDateTime.now());
        image.setUpdatedAt(LocalDateTime.now());
        productImageMapper.insert(image);
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }

    private Map<String, Object> toListItem(Product p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("title", p.getTitle());
        m.put("price", p.getPrice());
        m.put("first_image", fileStorageService.toUrl(p.getFirstImage()));
        m.put("category_name", p.getCategoryName());
        return m;
    }

    private Map<String, Object> toImageView(ProductImage img) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", img.getId());
        m.put("image", fileStorageService.toUrl(img.getImage()));
        m.put("created_at", img.getCreatedAt());
        return m;
    }

    private Map<String, Object> toProductPayload(Long id) {
        Product product = requireProduct(id);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", product.getId());
        m.put("title", product.getTitle());
        m.put("description", product.getDescription());
        m.put("price", product.getPrice());
        m.put("category", product.getCategoryId());
        m.put("seller", product.getSellerId());
        m.put("contact_info", product.getContactInfo());
        m.put("status", product.getStatus());
        m.put("created_at", product.getCreatedAt());
        m.put("images", productImageMapper.findByProductId(id).stream().map(this::toImageView).collect(Collectors.toList()));
        return m;
    }
}
