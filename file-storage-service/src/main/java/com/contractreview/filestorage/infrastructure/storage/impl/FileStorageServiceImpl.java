package com.contractreview.filestorage.infrastructure.storage.impl;

import com.contractreview.filestorage.domain.model.FileResource;
import com.contractreview.filestorage.domain.service.EncryptionService;
import com.contractreview.filestorage.domain.service.FileStorageService;
import com.contractreview.filestorage.infrastructure.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 文件存储服务实现
 * 
 * @author ContractReview Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final StorageProvider storageProvider;
    private final EncryptionService encryptionService;

    @Override
    public void storeFile(FileResource fileResource, MultipartFile file) {
        try {
            String bucketName = fileResource.getStorageLocation().getBucketName();
            String objectKey = fileResource.getObjectKey();
            
            storageProvider.uploadFile(
                bucketName,
                objectKey,
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
            );
            
            log.info("文件存储成功: uuid={}, fileName={}", 
                fileResource.getAttachmentUuid().getValue(), 
                fileResource.getMetadata().getFileName());
                
        } catch (IOException e) {
            log.error("文件存储失败: uuid={}", fileResource.getAttachmentUuid().getValue(), e);
            throw new RuntimeException("文件存储失败", e);
        }
    }

    @Override
    public void storeEncryptedFile(FileResource fileResource, MultipartFile file, String publicKey) {
        try {
            // 读取文件内容
            byte[] fileData = file.getBytes();
            
            // 加密文件内容
            byte[] encryptedData = encryptionService.encrypt(fileData, publicKey);
            
            String bucketName = fileResource.getStorageLocation().getBucketName();
            String objectKey = fileResource.getObjectKey();
            
            // 存储加密后的文件
            storageProvider.uploadFile(
                bucketName,
                objectKey,
                new ByteArrayInputStream(encryptedData),
                encryptedData.length,
                file.getContentType()
            );
            
            log.info("加密文件存储成功: uuid={}, fileName={}", 
                fileResource.getAttachmentUuid().getValue(), 
                fileResource.getMetadata().getFileName());
                
        } catch (IOException e) {
            log.error("加密文件存储失败: uuid={}", fileResource.getAttachmentUuid().getValue(), e);
            throw new RuntimeException("加密文件存储失败", e);
        }
    }

    @Override
    public byte[] retrieveFile(FileResource fileResource) {
        String bucketName = fileResource.getStorageLocation().getBucketName();
        String objectKey = fileResource.getObjectKey();
        
        byte[] fileData = storageProvider.downloadFile(bucketName, objectKey);
        
        log.info("文件获取成功: uuid={}, size={}", 
            fileResource.getAttachmentUuid().getValue(), fileData.length);
            
        return fileData;
    }

    @Override
    public byte[] retrieveAndDecryptFile(FileResource fileResource, String publicKey) {
        String bucketName = fileResource.getStorageLocation().getBucketName();
        String objectKey = fileResource.getObjectKey();
        
        // 获取加密文件
        byte[] encryptedData = storageProvider.downloadFile(bucketName, objectKey);
        
        // 解密文件内容
        byte[] decryptedData = encryptionService.decrypt(encryptedData, publicKey);
        
        log.info("加密文件获取并解密成功: uuid={}, encryptedSize={}, decryptedSize={}", 
            fileResource.getAttachmentUuid().getValue(), encryptedData.length, decryptedData.length);
            
        return decryptedData;
    }

    @Override
    public void deleteFile(FileResource fileResource) {
        String bucketName = fileResource.getStorageLocation().getBucketName();
        String objectKey = fileResource.getObjectKey();

        storageProvider.deleteFile(bucketName, objectKey);

        log.info("文件删除成功: uuid={}, fileName={}",
            fileResource.getAttachmentUuid().getValue(),
            fileResource.getMetadata().getFileName());
    }

    @Override
    public String generatePreviewUrl(FileResource fileResource, int expireSeconds) {
        String bucketName = fileResource.getStorageLocation().getBucketName();
        String objectKey = fileResource.getObjectKey();
        
        log.info("生成预览URL: uuid={}, fileName={}, expireSeconds={}", 
            fileResource.getAttachmentUuid().getValue(), 
            fileResource.getMetadata().getFileName(),
            expireSeconds);
            
        return storageProvider.generatePresignedUrl(bucketName, objectKey, expireSeconds);
    }
}