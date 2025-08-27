package com.solsolhey.util;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.solsolhey.solsol.config.MediaStorageProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaStorageService {
    private final MediaStorageProperties props;

    public static class SavedMedia {
        public final String urlPath; // e.g., /uploads/snapshots/2025/08/uuid.png
        public final Path filePath;
        public SavedMedia(String urlPath, Path filePath) { this.urlPath = urlPath; this.filePath = filePath; }
    }

    public SavedMedia saveImageDataUrl(String dataUrl, String category) {
        if (dataUrl == null || !dataUrl.startsWith("data:image/")) {
            throw new IllegalArgumentException("Unsupported image data URL");
        }
        String ext = dataUrl.startsWith("data:image/jpeg") || dataUrl.startsWith("data:image/jpg") ? ".jpg" : ".png";
        int comma = dataUrl.indexOf(',');
        if (comma < 0) throw new IllegalArgumentException("Invalid data URL");
        String b64 = dataUrl.substring(comma + 1);
        byte[] bytes = Base64.getDecoder().decode(b64);

        String datePath = DateTimeFormatter.ofPattern("yyyy/MM").format(LocalDateTime.now());
        String fname = UUID.randomUUID().toString().replaceAll("-", "") + ext;
        Path root = Paths.get(props.getUploadDir()).toAbsolutePath().normalize();
        Path dir = root.resolve(category).resolve(datePath);
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(fname);
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                fos.write(bytes);
            }
            String url = "/uploads/" + category + "/" + datePath + "/" + fname;
            return new SavedMedia(url, file);
        } catch (Exception e) {
            log.error("Failed to save image", e);
            throw new RuntimeException("Failed to save image", e);
        }
    }

    // URL 경로(/uploads/...)를 파일 시스템 경로로 변환해 삭제
    public boolean deleteUrlPath(String urlPath) {
        try {
            if (urlPath == null || !urlPath.startsWith("/uploads/")) return false;
            String relative = urlPath.substring("/uploads/".length());
            Path root = Paths.get(props.getUploadDir()).toAbsolutePath().normalize();
            Path file = root.resolve(relative);
            if (Files.exists(file)) {
                Files.delete(file);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to delete media file: {}", urlPath, e);
            return false;
        }
    }
}
