package com.ute.trasua.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for media asset storage on Cloudinary.
 * Follows strict interface segregation as required by project conventions.
 */
public interface CloudinaryService {

    /**
     * Upload an image file synchronously to Cloudinary.
     *
     * @param file   the multipart file to upload
     * @param folder the target folder on Cloudinary (e.g., "products", "branches")
     * @return secure HTTPS URL of the uploaded image
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Upload an image file asynchronously to Cloudinary.
     *
     * @param file   the multipart file to upload
     * @param folder the target folder on Cloudinary
     * @return CompletableFuture holding the secure HTTPS URL
     */
    CompletableFuture<String> uploadImageAsync(MultipartFile file, String folder);

    /**
     * Upload an image file and return complete response metadata.
     *
     * @param file   the multipart file to upload
     * @param folder the target folder on Cloudinary
     * @return map of response attributes including url, public_id, format, width, height, etc.
     */
    Map<String, Object> uploadImageWithDetails(MultipartFile file, String folder);

    /**
     * Delete an image from Cloudinary by its public ID.
     *
     * @param publicId the public ID of the resource
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteImage(String publicId);

    /**
     * Extract public ID from a Cloudinary URL.
     *
     * @param imageUrl full Cloudinary URL
     * @return public ID or empty string if not found
     */
    String extractPublicId(String imageUrl);

    /**
     * Upload an image from base64 data string (with fallback).
     *
     * @param base64Data base64 data URI string or raw base64
     * @param folder target folder (e.g. "deliveries")
     * @return secure image URL or original data URI fallback
     */
    String uploadBase64Image(String base64Data, String folder);
}
