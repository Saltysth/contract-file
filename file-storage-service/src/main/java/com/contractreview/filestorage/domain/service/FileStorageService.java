package com.contractreview.filestorage.domain.service;

import com.contractreview.filestorage.domain.model.FileResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储领域服务
 * 
 * @author ContractReview Team
 */
public interface FileStorageService {

    /**
     * 存储文件到MinIO
     */
    void storeFile(FileResource fileResource, MultipartFile file);

    /**
     * 存储加密文件到MinIO
     */
    void storeEncryptedFile(FileResource fileResource, MultipartFile file, String publicKey);

    /**
     * 从MinIO获取文件
     */
    byte[] retrieveFile(FileResource fileResource);

    /**
     * 从MinIO获取加密文件并解密
     */
    byte[] retrieveAndDecryptFile(FileResource fileResource, String publicKey);

    /**
     * 从MinIO删除文件
     */
    void deleteFile(FileResource fileResource);

    /**
     * 生成文件预览URL（可直接在浏览器访问）
     */
    String generatePreviewUrl(FileResource fileResource, int expireSeconds);
}