package com.eduwork.identity.infrastructure.storage;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * MinIO implementation of FileStorageService.
 * MinIO is S3-compatible object storage.
 * 
 * Performance: Async uploads recommended for large files
 * Development: Uses Docker MinIO instance
 * Production: Can point to AWS S3 or any S3-compatible service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name:eduwork-kyc}")
    private String bucketName;

    @Value("${minio.url:http://localhost:9000}")
    private String minioUrl;

    /**
     * Initialize bucket on startup.
     */
    @PostConstruct
    public void init() {
        try {
            // Check if bucket exists, create if not
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket: {}", bucketName);
            } else {
                log.info("MinIO bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
            throw new RuntimeException("MinIO initialization failed", e);
        }
    }

    @Override
    public String store(MultipartFile file, UUID userId, String folder) {
        try {
            // Generate unique object name: folder/userId/timestamp_filename
            String objectName = String.format("%s/%s/%d_%s",
                    folder,
                    userId,
                    System.currentTimeMillis(),
                    file.getOriginalFilename());

            // Upload to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            log.info("Uploaded file to MinIO: {}", objectName);

            // Return URL: http://localhost:9000/bucket/objectName
            return String.format("%s/%s/%s", minioUrl, bucketName, objectName);

        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public InputStream retrieve(String fileUrl) {
        try {
            // Extract object name from URL
            String objectName = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            log.error("Failed to retrieve file from MinIO: {}", fileUrl, e);
            throw new RuntimeException("File retrieval failed", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            // Extract object name from URL
            String objectName = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

            log.info("Deleted file from MinIO: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", fileUrl, e);
            throw new RuntimeException("File deletion failed", e);
        }
    }
}
