package com.contractreview.filestorage.infrastructure.storage.provider;

import com.contractreview.filestorage.domain.service.BucketNameValidator;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * MinIO存储提供者实现
 * 
 * @author ContractReview Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MinIOStorageProvider implements StorageProvider {

    private final MinioClient minioClient;

    @Override
    public void uploadFile(String bucketName, String objectKey, InputStream inputStream, long size, String contentType) {
        try {
            createBucketIfNotExists(bucketName);
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            );
            
            log.info("文件上传成功: bucket={}, objectKey={}", bucketName, objectKey);
        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, objectKey={}", bucketName, objectKey, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public byte[] downloadFile(String bucketName, String objectKey) {
        try {
            GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build()
            );
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = response.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            response.close();
            log.info("文件下载成功: bucket={}, objectKey={}", bucketName, objectKey);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("文件下载失败: bucket={}, objectKey={}", bucketName, objectKey, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public void deleteFile(String bucketName, String objectKey) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build()
            );
            
            log.info("文件删除成功: bucket={}, objectKey={}", bucketName, objectKey);
        } catch (Exception e) {
            log.error("文件删除失败: bucket={}, objectKey={}", bucketName, objectKey, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    @Override
    public boolean fileExists(String bucketName, String objectKey) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            log.error("检查文件存在性失败: bucket={}, objectKey={}", bucketName, objectKey, e);
            throw new RuntimeException("检查文件存在性失败", e);
        } catch (Exception e) {
            log.error("检查文件存在性失败: bucket={}, objectKey={}", bucketName, objectKey, e);
            throw new RuntimeException("检查文件存在性失败", e);
        }
    }

    @Override
    public void createBucketIfNotExists(String bucketName) {
        // 校验bucket名称是否符合S3标准
        BucketNameValidator.ValidationResult validationResult = BucketNameValidator.validate(bucketName);
        if (!validationResult.isValid()) {
            log.error("Bucket名称不符合S3标准: {}, 错误信息: {}", bucketName, validationResult.getMessage());
            throw new IllegalArgumentException("Bucket名称不符合S3标准: " + validationResult.getMessage());
        }
        
        // 使用标准化后的bucket名称
        String normalizedBucketName = validationResult.getNormalizedName();
        
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(normalizedBucketName)
                    .build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(normalizedBucketName)
                        .build()
                );
                log.info("存储桶创建成功: {} (原名称: {})", normalizedBucketName, bucketName);
            }
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", normalizedBucketName, e);
            throw new RuntimeException("创建存储桶失败", e);
        }
    }

    @Override
    public String generatePresignedUrl(String bucketName, String objectKey, int expireSeconds) {
        try {
            String presignedUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(expireSeconds)
                    .build()
            );
            
            log.info("生成预签名URL成功: bucket={}, objectKey={}, expireSeconds={}", 
                bucketName, objectKey, expireSeconds);
            return presignedUrl;
            
        } catch (Exception e) {
            log.error("生成预签名URL失败: bucket={}, objectKey={}", bucketName, objectKey, e);
            throw new RuntimeException("生成预签名URL失败", e);
        }
    }
}