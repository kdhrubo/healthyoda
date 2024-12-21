package com.healthyoda.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class AudioStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudioStorageService.class);

    private final ResourcePatternResolver resourcePatternResolver;

    @Value("${audio.upload.dir}")
    private String uploadDir;

    public AudioStorageService(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public final List<Resource> getAllFiles() {
        Resource[] resources = null;
        try {
            resources = resourcePatternResolver
                    .getResources("file:" + uploadDir + "/*");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.asList(resources);

    }

    public String saveAudio(MultipartFile audioFile, String fileName) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save the file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(audioFile.getInputStream(), filePath);

        LOGGER.info("Saved audio file: {}", filePath.toString());
        
        return filePath.toString();
    }
} 