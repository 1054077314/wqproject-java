package com.campus.product.service;

import com.campus.appointment.mapper.AppointmentMapper;
import com.campus.audit.service.AuditService;
import com.campus.category.mapper.CategoryMapper;
import com.campus.comment.mapper.CommentMapper;
import com.campus.comment.vo.CommentVo;
import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.PageUtils;
import com.campus.config.AppProperties;
import com.campus.config.CacheConfig;
import com.campus.favorite.mapper.FavoriteMapper;
import com.campus.product.dto.ProductCreateRequest;
import com.campus.product.dto.ProductReviewRequest;
import com.campus.product.dto.ProductUpdateRequest;
import com.campus.product.entity.Product;
import com.campus.product.entity.ProductImage;
import com.campus.product.mapper.ProductImageMapper;
import com.campus.product.mapper.ProductMapper;
import com.campus.product.vo.MyProductVo;
import com.campus.product.vo.ProductDetailVo;
import com.campus.product.vo.ProductImageVo;
import com.campus.product.vo.ProductListItemVo;
import com.campus.product.vo.ProductPayloadVo;
import com.campus.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final AuditService auditService;

    public ProductService(ProductMapper productMapper, ProductImageMapper productImageMapper,
                          CategoryMapper categoryMapper, AppointmentMapper appointmentMapper,
                          FavoriteMapper favoriteMapper, CommentMapper commentMapper,
                          FileStorageService fileStorageService, AppProperties appProperties,
                          AuditService auditService) {
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.categoryMapper = categoryMapper;
        this.appointmentMapper = appointmentMapper;
        this.favoriteMapper = favoriteMapper;
        this.commentMapper = commentMapper;
        this.fileStorageService = fileStorageService;
        this.appProperties = appProperties;
        this.auditService = auditService;
    }

    public PageResult<ProductListItemVo> listActive(HttpServletRequest request, Long categoryId, String search) {
        return PageUtils.paginateMapped(request, appProperties.getPageSize(),
                () -> productMapper.findActive(categoryId, search),
                rows -> rows.stream().map(this::toListItem).collect(Collectors.toList()));
    }

    public ProductDetailVo detail(Long id, UserPrincipal principal) {
        Product product = requireProduct(id);
        boolean ownerOrAdmin = principal != null
                && (principal.getId().equals(product.getSellerId()) || principal.isStaff());
        boolean publicVisible = "active".equals(product.getStatus()) || "sold".equals(product.getStatus());
        if (!publicVisible && !ownerOrAdmin) {
            throw new BusinessException(404, "商品不存在");
        }
        ProductDetailVo vo = new ProductDetailVo();
        vo.setId(product.getId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setImages(productImageMapper.findByProductId(id).stream().map(this::toImageVo).collect(Collectors.toList()));
        vo.setCategoryName(product.getCategoryName());
        vo.setContactInfo(product.getContactInfo());
        vo.setStatus(product.getStatus());
        vo.setCreatedAt(product.getCreatedAt());
        vo.setSellerUsername(product.getSellerUsername());
        vo.setSellerId(product.getSellerId());
        vo.setComments(commentMapper.findByProductId(id).stream().map(c -> {
            CommentVo cv = new CommentVo();
            cv.setId(c.getId());
            cv.setContent(c.getContent());
            cv.setUsername(c.getUsername());
            cv.setCreatedAt(c.getCreatedAt());
            return cv;
        }).collect(Collectors.toList()));
        vo.setAppointmentCount(appointmentMapper.countByProduct(id));
        if (principal != null) {
            vo.setFavorited(favoriteMapper.exists(principal.getId(), id));
            vo.setAppointed(appointmentMapper.exists(principal.getId(), id));
        }
        return vo;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.STATISTICS, allEntries = true)
    public ProductPayloadVo create(ProductCreateRequest request, List<MultipartFile> files, UserPrincipal principal) {
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
    @CacheEvict(cacheNames = CacheConfig.STATISTICS, allEntries = true)
    public ProductPayloadVo update(Long id, ProductUpdateRequest request, List<MultipartFile> files, UserPrincipal principal) {
        Product product = requireProduct(id);
        if (!product.getSellerId().equals(principal.getId())) {
            throw new BusinessException(403, "无权操作此商品");
        }
        if ("active".equals(product.getStatus())) {
            throw new BusinessException(400, "已上架商品需先下架再编辑");
        }
        if ("sold".equals(product.getStatus())) {
            throw new BusinessException(400, "已售出商品不可编辑");
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
                fileStorageService.deleteQuietly(img.getImage());
            }
        }
        saveImages(id, files, request.getUploadedImages());
        return toProductPayload(id);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.STATISTICS, allEntries = true)
    public void offline(Long id, UserPrincipal principal) {
        Product product = requireProduct(id);
        if (!product.getSellerId().equals(principal.getId())) {
            throw new BusinessException(403, "无权操作此商品");
        }
        if (appointmentMapper.countPendingByProduct(id) > 0) {
            throw new BusinessException(400, "需先处理完待确认预约");
        }
        if (productMapper.casStatus(id, "active", "offline", product.getRejectReason()) == 0) {
            throw new BusinessException(409, "仅在售商品可下架，或状态已变更");
        }
        auditService.record(principal, "product.offline", "product", id, null);
    }

    public PageResult<MyProductVo> myProducts(HttpServletRequest request, String status, UserPrincipal principal) {
        return PageUtils.paginateMapped(request, appProperties.getPageSize(),
                () -> productMapper.findBySeller(principal.getId(), status),
                products -> {
                    Map<Long, List<ProductImage>> imagesByProduct = loadImagesGrouped(
                            products.stream().map(Product::getId).collect(Collectors.toList()));
                    return products.stream().map(p -> {
                MyProductVo vo = new MyProductVo();
                vo.setId(p.getId());
                vo.setTitle(p.getTitle());
                vo.setPrice(p.getPrice());
                vo.setStatus(p.getStatus());
                List<ProductImage> images = imagesByProduct.getOrDefault(p.getId(), List.of());
                vo.setImages(images.stream().map(this::toImageVo).collect(Collectors.toList()));
                vo.setCategoryName(p.getCategoryName());
                vo.setAppointmentCount(p.getAppointmentCount() == null ? 0 : p.getAppointmentCount());
                vo.setCreatedAt(p.getCreatedAt());
                return vo;
            }).collect(Collectors.toList());
        });
    }

    private Map<Long, List<ProductImage>> loadImagesGrouped(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productImageMapper.findByProductIds(productIds).stream()
                .collect(Collectors.groupingBy(ProductImage::getProductId, LinkedHashMap::new, Collectors.toList()));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.STATISTICS, allEntries = true)
    public void review(Long id, ProductReviewRequest request, UserPrincipal principal) {
        Product product = requireProduct(id);
        if ("approve".equals(request.getAction())) {
            if (productMapper.casStatus(id, "pending", "active", "") == 0) {
                throw new BusinessException(409, "只能审核待审核商品，或状态已变更");
            }
            auditService.record(principal, "product.approve", "product", id, null);
            return;
        }
        if ("reject".equals(request.getAction())) {
            String reason = request.getRejectReason() == null ? "" : request.getRejectReason().trim();
            if (reason.isEmpty()) {
                throw new BusinessException(400, "驳回原因不能为空");
            }
            if (productMapper.casStatus(id, "pending", "rejected", reason) == 0) {
                throw new BusinessException(409, "只能审核待审核商品，或状态已变更");
            }
            auditService.record(principal, "product.reject", "product", id, reason);
            return;
        }
        throw new BusinessException(400, "无效操作");
    }

    public PageResult<ProductListItemVo> pendingList(HttpServletRequest request) {
        return PageUtils.paginateMapped(request, appProperties.getPageSize(),
                productMapper::findPending,
                rows -> rows.stream().map(this::toListItem).collect(Collectors.toList()));
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

    private ProductListItemVo toListItem(Product p) {
        ProductListItemVo vo = new ProductListItemVo();
        vo.setId(p.getId());
        vo.setTitle(p.getTitle());
        vo.setPrice(p.getPrice());
        vo.setFirstImage(fileStorageService.toUrl(p.getFirstImage()));
        vo.setCategoryName(p.getCategoryName());
        return vo;
    }

    private ProductImageVo toImageVo(ProductImage img) {
        ProductImageVo vo = new ProductImageVo();
        vo.setId(img.getId());
        vo.setImage(fileStorageService.toUrl(img.getImage()));
        vo.setCreatedAt(img.getCreatedAt());
        return vo;
    }

    private ProductPayloadVo toProductPayload(Long id) {
        Product product = requireProduct(id);
        ProductPayloadVo vo = new ProductPayloadVo();
        vo.setId(product.getId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setCategory(product.getCategoryId());
        vo.setSeller(product.getSellerId());
        vo.setContactInfo(product.getContactInfo());
        vo.setStatus(product.getStatus());
        vo.setCreatedAt(product.getCreatedAt());
        vo.setImages(productImageMapper.findByProductId(id).stream().map(this::toImageVo).collect(Collectors.toList()));
        return vo;
    }
}
