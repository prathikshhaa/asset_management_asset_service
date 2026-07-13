package com.assetmanagement.assetservice.service;

import com.assetmanagement.assetservice.exception.FileProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("uploads");

    public String store(MultipartFile file) {

        try {

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String extension =
                    StringUtils.getFilenameExtension(file.getOriginalFilename());

            String fileName =
                    UUID.randomUUID() + "." + extension;

            Files.copy(file.getInputStream(),
                    uploadDir.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + fileName;

        } catch (IOException e) {
            throw new FileProcessingException("Unable to upload image", e);
        }
    }
}