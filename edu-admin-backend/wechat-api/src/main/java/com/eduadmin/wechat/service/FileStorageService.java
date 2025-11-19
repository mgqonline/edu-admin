package com.eduadmin.wechat.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path baseDir;

    public FileStorageService() {
        this.baseDir = new File("uploads").toPath();
        try { Files.createDirectories(baseDir); } catch (Exception ignore) { /* no-op */ }
    }

    public String store(String subDir, MultipartFile file) throws IOException {
        String original = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : UUID.randomUUID() + "";
        Path dir = baseDir.resolve(subDir);
        Files.createDirectories(dir);
        Path target = dir.resolve(original);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        // Public URL mapped via WebConfig: /files/** -> uploads/
        String urlPath = "/files/" + subDir + "/" + original;
        return urlPath;
    }
}