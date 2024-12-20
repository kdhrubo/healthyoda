package com.healthyoda.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class AudioStorageService {
    private final Path rootLocation;

    @Autowired
    public AudioStorageService(@Value("${audio.upload.dir}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public void store(MultipartFile file, String fileName) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + fileName);
            }
            
            Path destinationFile = this.rootLocation.resolve(
                    Paths.get(fileName))
                    .normalize()
                    .toAbsolutePath();
                    
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + fileName, e);
        }
    }
} 