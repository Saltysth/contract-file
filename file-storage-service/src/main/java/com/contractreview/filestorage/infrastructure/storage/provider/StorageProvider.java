package com.contractreview.filestorage.infrastructure.storage.provider;

import java.io.InputStream;

/**
 * 存储提供者接口
 * 
 * @author ContractReview Team
 */
public interface StorageProvider {

    /**
     * 上传文件
     */
    void uploadFile(String bucketName, String objectKey, InputStream inputStream, long size, String contentType);

    /**
     * 下载文件
     */
    byte[] downloadFile(String bucketName, String objectKey);

    /**
     * 删除文件
     */
    void deleteFile(String bucketName, String objectKey);

    /**
     * 检查文件是否存在
     */
    boolean fileExists(String bucketName, String objectKey);

    /**
     * 创建存储桶（如果不存在）
     */
    void createBucketIfNotExists(String bucketName);

    /**
     * 生成预签名下载URL（可直接在浏览器预览）
     * 
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param expireSeconds 过期时间（秒）
     * @return 预签名URL
     */
    String generatePresignedUrl(String bucketName, String objectKey, int expireSeconds);
}