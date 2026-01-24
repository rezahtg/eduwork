package com.eduwork.identity.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * Interface for file storage operations.
 * Abstraction over MinIO, S3, or local storage.
 */
public interface FileStorageService {

    /**
     * Store uploaded file.
     * 
     * @param file   uploaded file
     * @param userId user ID (for organizing files)
     * @param folder folder/prefix (e.g., "kyc")
     * @return file URL for retrieval
     */
    String store(MultipartFile file, UUID userId, String folder);

    /**
     * Retrieve file as input stream.
     * 
     * @param fileUrl file URL from storage
     * @return file content stream
     */
    InputStream retrieve(String fileUrl);

    /**
     * Delete file.
     * 
     * @param fileUrl file URL to delete
     */
    void delete(String fileUrl);
}
