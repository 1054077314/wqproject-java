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
import java.util.UUID;

@Service
public class FileStorageService {

    private final AppProperties appProperties;

    public FileStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "图片不能为空");
        }
        String original = file.getOriginalFilename() == null ? "image.jpg" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".jpg";
        String filename = "products/" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = Paths.get(appProperties.getMediaRoot()).resolve(filename).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dest.getParent());
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(500, "图片保存失败");
        }
        return filename.replace('\\', '/');
    }

    public String storeBase64(String dataUrl) {
        if (dataUrl == null || !dataUrl.contains(",")) {
            throw new BusinessException(400, "图片格式错误");
        }
        String meta = dataUrl.substring(0, dataUrl.indexOf(','));
        String base64 = dataUrl.substring(dataUrl.indexOf(',') + 1);
        String ext = ".jpg";
        if (meta.contains("image/png")) {
            ext = ".png";
        } else if (meta.contains("image/webp")) {
            ext = ".webp";
        } else if (meta.contains("image/gif")) {
            ext = ".gif";
        }
        byte[] bytes = Base64.getDecoder().decode(base64);
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
}
