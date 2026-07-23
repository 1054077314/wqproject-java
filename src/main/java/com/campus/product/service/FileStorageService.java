package com.campus.product.service;

import com.campus.common.BusinessException;
import com.campus.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private final AppProperties appProperties;

    public FileStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "图片不能为空");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(400, "仅支持 JPEG/PNG/GIF/WebP 图片");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(500, "图片读取失败");
        }
        String ext = detectExtension(bytes);
        return writeBytes(bytes, ext);
    }

    public String storeBase64(String dataUrl) {
        if (dataUrl == null || !dataUrl.contains(",")) {
            throw new BusinessException(400, "图片格式错误");
        }
        String meta = dataUrl.substring(0, dataUrl.indexOf(','));
        String base64 = dataUrl.substring(dataUrl.indexOf(',') + 1);
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "图片格式错误");
        }
        if (meta.toLowerCase(Locale.ROOT).contains("image/")) {
            boolean allowedMeta = ALLOWED_CONTENT_TYPES.stream().anyMatch(meta.toLowerCase(Locale.ROOT)::contains);
            if (!allowedMeta) {
                throw new BusinessException(400, "仅支持 JPEG/PNG/GIF/WebP 图片");
            }
        }
        String ext = detectExtension(bytes);
        return writeBytes(bytes, ext);
    }

    public String toUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        if (relativePath.startsWith("http") || relativePath.startsWith("/")) {
            return relativePath;
        }
        String prefix = appProperties.getMediaUrl();
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + relativePath;
    }

    /** Best-effort delete of a stored relative media path. */
    public void deleteQuietly(String relativePath) {
        if (relativePath == null || relativePath.isBlank()
                || relativePath.startsWith("http") || relativePath.startsWith("/")) {
            return;
        }
        Path dest = Paths.get(appProperties.getMediaRoot()).resolve(relativePath).toAbsolutePath().normalize();
        Path root = Paths.get(appProperties.getMediaRoot()).toAbsolutePath().normalize();
        if (!dest.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(dest);
        } catch (IOException ignored) {
            // disk cleanup should not fail the business transaction
        }
    }

    private String writeBytes(byte[] bytes, String ext) {
        String filename = "products/" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = Paths.get(appProperties.getMediaRoot()).resolve(filename).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dest.getParent());
            Files.write(dest, bytes);
        } catch (IOException e) {
            throw new BusinessException(500, "图片保存失败");
        }
        return filename.replace('\\', '/');
    }

    /** Resolve extension from magic bytes, not client-supplied filename. */
    static String detectExtension(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            throw new BusinessException(400, "图片内容无效");
        }
        // JPEG
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return ".jpg";
        }
        // PNG
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return ".png";
        }
        // GIF
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8') {
            return ".gif";
        }
        // WEBP: RIFF....WEBP
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return ".webp";
        }
        throw new BusinessException(400, "不支持的图片格式");
    }
}
