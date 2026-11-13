package com.skyspireinfotech.admin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Saves uploaded media into app.upload-dir (served at /uploads/**) and removes
 * the file it replaces. Only whitelisted content types are accepted, and the
 * stored filename is always a fresh UUID — never the client's filename.
 */
@Service
public class MediaStorage {

    public static final Map<String, String> IMAGE_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    public static final Map<String, String> VIDEO_TYPES = Map.of(
            "video/mp4", ".mp4",
            "video/webm", ".webm");

    @Value("${app.upload-dir}")
    private String uploadDir;

    /**
     * @return the stored file's URL ("/uploads/..."), relative to the app's context path
     * @throws IllegalArgumentException with a user-facing message if the file is rejected
     */
    public String store(MultipartFile file, Map<String, String> allowedTypes, long maxBytes, String previousUrl)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a file to upload.");
        }
        String extension = allowedTypes.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("That file type isn't supported here.");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("That file is too large (max " + (maxBytes / (1024 * 1024)) + " MB).");
        }

        Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        String filename = UUID.randomUUID() + extension;
        file.transferTo(dir.resolve(filename));

        delete(previousUrl);
        return "/uploads/" + filename;
    }

    /** Deletes a previously stored upload; ignores anything that isn't one of ours. */
    public void delete(String url) throws IOException {
        if (url == null || !url.startsWith("/uploads/")) {
            return;
        }
        Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
        Path target = dir.resolve(url.substring("/uploads/".length())).normalize();
        if (target.startsWith(dir)) {
            Files.deleteIfExists(target);
        }
    }
}
