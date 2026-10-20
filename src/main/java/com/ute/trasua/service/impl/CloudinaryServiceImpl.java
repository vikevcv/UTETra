package com.ute.trasua.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ute.trasua.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of CloudinaryService for managing media assets on Cloudinary.
 */
@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(@org.springframework.beans.factory.annotation.Autowired(required = false) Cloudinary cloudinary) {
        super();
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        Map<String, Object> uploadResult = uploadImageWithDetails(file, folder);
        String secureUrl = (String) uploadResult.get("secure_url");
        if (secureUrl == null || secureUrl.isBlank()) {
            secureUrl = (String) uploadResult.get("url");
        }
        return secureUrl;
    }

    @Async
    @Override
    public CompletableFuture<String> uploadImageAsync(MultipartFile file, String folder) {
        String url = uploadImage(file, folder);
        return CompletableFuture.completedFuture(url);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImageWithDetails(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload file must not be null or empty!");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type: [" + contentType + "]. Only image files are allowed!");
        }

        Map<String, Object> options = new HashMap<>();
        if (folder != null && !folder.trim().isEmpty()) {
            options.put("folder", folder.trim());
        }
        options.put("resource_type", "image");

        try {
            log.info(">>> Uploading image to Cloudinary (folder: {}, originalName: {}, size: {} bytes)...",
                    folder, file.getOriginalFilename(), file.getSize());

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            log.info(">>> Image uploaded successfully. Public ID: {}, URL: {}",
                    uploadResult.get("public_id"), uploadResult.get("secure_url"));

            return uploadResult;
        } catch (IOException e) {
            log.error(">>> Failed to upload file to Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteImage(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) {
            log.warn(">>> Attempted to delete image with null or empty public ID.");
            return false;
        }

        try {
            log.info(">>> Deleting image on Cloudinary with public ID: [{}]", publicId);
            Map result = cloudinary.uploader().destroy(publicId.trim(), ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");
            boolean success = "ok".equalsIgnoreCase(resultStatus);

            if (success) {
                log.info(">>> Successfully deleted image on Cloudinary: [{}]", publicId);
            } else {
                log.warn(">>> Cloudinary deletion returned non-ok status: [{}] for public ID: [{}]", resultStatus, publicId);
            }
            return success;
        } catch (Exception e) {
            log.error(">>> Error deleting image on Cloudinary [{}]: {}", publicId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty() || !imageUrl.contains("cloudinary.com")) {
            return "";
        }

        int uploadIdx = imageUrl.indexOf("/upload/");
        if (uploadIdx == -1) {
            return "";
        }

        String pathAfterUpload = imageUrl.substring(uploadIdx + "/upload/".length());

        // Strip version number prefix like v1234567890/
        if (pathAfterUpload.matches("^v\\d+/.*")) {
            pathAfterUpload = pathAfterUpload.replaceFirst("^v\\d+/", "");
        }

        // Strip file extension (.jpg, .png, .webp, etc.)
        int lastDotIdx = pathAfterUpload.lastIndexOf('.');
        if (lastDotIdx != -1) {
            pathAfterUpload = pathAfterUpload.substring(0, lastDotIdx);
        }

        return pathAfterUpload;
    }

    @Override
    public String uploadBase64Image(String base64Data, String folder) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return null;
        }
        // Nếu đã là URL Cloudinary hoặc HTTP URL thì giữ nguyên
        if (base64Data.startsWith("http://") || base64Data.startsWith("https://")) {
            return base64Data;
        }
        try {
            if (cloudinary != null) {
                Map<String, Object> options = ObjectUtils.asMap(
                        "folder", "utetra/" + folder,
                        "resource_type", "image"
                );
                Map uploadResult = cloudinary.uploader().upload(base64Data, options);
                if (uploadResult != null && uploadResult.get("secure_url") != null) {
                    return uploadResult.get("secure_url").toString();
                }
            }
        } catch (Exception e) {
            log.warn(">>> Cloudinary upload failed, falling back to base64 data URI: {}", e.getMessage());
        }
        // ponytail: fallback an toàn giữ nguyên base64 data URI nếu Cloudinary mất kết nối
        return base64Data;
    }
}
