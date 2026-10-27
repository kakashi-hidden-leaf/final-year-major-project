package com.campus360.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.vault-dir:./uploads/vault/}")
    private String vaultDir;

    @Value("${app.upload.assignments-dir:./uploads/assignments/}")
    private String assignmentsDir;

    @Value("${app.upload.issues-dir:./uploads/issues/}")
    private String issuesDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(vaultDir));
            Files.createDirectories(Paths.get(assignmentsDir));
            Files.createDirectories(Paths.get(issuesDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage directories!", e);
        }
    }

    public String storeVaultFile(MultipartFile file) throws IOException {
        return storeFile(file, Paths.get(vaultDir));
    }

    public String storeAssignmentFile(MultipartFile file) throws IOException {
        return storeFile(file, Paths.get(assignmentsDir));
    }

    public String storeIssueProofFile(MultipartFile file) throws IOException {
        return storeFile(file, Paths.get(issuesDir));
    }

    private String storeFile(MultipartFile file, Path targetLocation) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        String safeFileName = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path destination = targetLocation.resolve(safeFileName).normalize().toAbsolutePath();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        return safeFileName;
    }

    public Resource loadFileAsResource(String subDirectory, String fileName) {
        try {
            Path basePath;
            if ("vault".equalsIgnoreCase(subDirectory)) {
                basePath = Paths.get(vaultDir);
            } else if ("assignments".equalsIgnoreCase(subDirectory)) {
                basePath = Paths.get(assignmentsDir);
            } else if ("issues".equalsIgnoreCase(subDirectory)) {
                basePath = Paths.get(issuesDir);
            } else {
                basePath = Paths.get("./uploads/");
            }

            Path filePath = basePath.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or unreadable: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File path invalid: " + fileName, ex);
        }
    }
}
