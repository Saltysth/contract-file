package com.contractreview.filestorage.application.service;

import com.contractreview.filestorage.domain.model.FileResource;
import com.contractreview.filestorage.domain.repository.FileResourceRepository;
import com.contractreview.filestorage.domain.service.EncryptionService;
import com.contractreview.filestorage.domain.service.FileStorageService;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileInfoResponse;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * URL操作应用服务
 * 
 * @author ContractReview Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlBasedFileService {

    private final FileResourceRepository fileResourceRepository;
    private final FileStorageService fileStorageService;
    private final EncryptionService encryptionService;

    /**
     * 通过URL上传文件
     */
    @Transactional
    public FileUploadResponse uploadByUrl(MultipartFile file, String bucketName, String publicKey, boolean needPreview) {
        // 验证参数
        validateUploadParams(file, bucketName);
        
        // 验证加密密钥（如果提供）
        boolean encrypted = StringUtils.isNotBlank(publicKey);
        if (encrypted && !encryptionService.validatePublicKey(publicKey)) {
            throw new IllegalArgumentException("加密密钥格式错误");
        }

        // 创建文件资源
        FileResource fileResource = FileResource.create(
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize(),
            bucketName,
            "URL_UPLOAD",
            encrypted
        );

        // 验证文件可以上传
        fileResource.validateForUpload();

        try {
            // 存储文件
            if (encrypted) {
                fileStorageService.storeEncryptedFile(fileResource, file, publicKey);
            } else {
                fileStorageService.storeFile(fileResource, file);
            }

            // 保存元数据
            FileResource savedResource = fileResourceRepository.save(fileResource);

            log.info("文件上传成功: uuid={}, fileName={}, encrypted={}", 
                savedResource.getAttachmentUuid().getValue(), 
                savedResource.getMetadata().getFileName(),
                encrypted);

            String previewUrl = needPreview ? fileStorageService.generatePreviewUrl(fileResource, 3600) : savedResource.getFileUrl();

            return FileUploadResponse.builder()
                .uuid(savedResource.getAttachmentUuid().getValue())
                .fileUrl(previewUrl)
                .fileName(savedResource.getMetadata().getFileName())
                .fileSize(savedResource.getMetadata().getFileSize())
                .fileType(savedResource.getMetadata().getFileType())
                .isEncrypted(savedResource.requiresEncryption())
                .timestamp(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("文件上传失败: fileName={}", file.getOriginalFilename(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过URL下载文件
     */
    public byte[] downloadByUrl(String fileUrl, String publicKey) {
        // 查找文件资源
        FileResource fileResource = fileResourceRepository.findByFileUrl(fileUrl)
            .orElseThrow(() -> new IllegalArgumentException("文件不存在"));

        try {
            // 验证文件访问
            fileResource.validateForAccess();

            // 下载文件
            if (fileResource.requiresDecryption()) {
                if (StringUtils.isBlank(publicKey)) {
                    throw new IllegalArgumentException("加密文件需要提供解密密钥");
                }
                if (!encryptionService.validatePublicKey(publicKey)) {
                    throw new IllegalArgumentException("解密密钥格式错误");
                }
                return fileStorageService.retrieveAndDecryptFile(fileResource, publicKey);
            } else {
                return fileStorageService.retrieveFile(fileResource);
            }

        } catch (Exception e) {
            log.error("文件下载失败: fileUrl={}", fileUrl, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过URL查询文件信息
     */
    public FileInfoResponse queryByUrl(String fileUrl) {
        FileResource fileResource = fileResourceRepository.findByFileUrl(fileUrl)
            .orElseThrow(() -> new IllegalArgumentException("文件不存在"));

        return FileInfoResponse.builder()
            .uuid(fileResource.getAttachmentUuid().getValue())
            .fileUrl(fileResource.getFileUrl())
            .fileName(fileResource.getMetadata().getFileName())
            .fileSize(fileResource.getMetadata().getFileSize())
            .fileType(fileResource.getMetadata().getFileType())
            .bucketName(fileResource.getStorageLocation().getBucketName())
            .directory(fileResource.getStorageLocation().getDirectory())
            .isEncrypted(fileResource.requiresEncryption())
            .createdTime(fileResource.getMetadata().getCreatedTime())
            .updatedTime(fileResource.getMetadata().getUpdatedTime())
            .build();
    }

    /**
     * 通过URL删除文件
     */
    @Transactional
    public void deleteByUrl(String fileUrl) {
        FileResource fileResource = fileResourceRepository.findByFileUrl(fileUrl)
            .orElseThrow(() -> new IllegalArgumentException("文件不存在"));

        try {
            // 从存储中删除文件
            fileStorageService.deleteFile(fileResource);

            // 从数据库删除记录
            fileResourceRepository.deleteByFileUrl(fileUrl);

            log.info("文件删除成功: fileUrl={}, uuid={}", 
                fileUrl, fileResource.getAttachmentUuid().getValue());

        } catch (Exception e) {
            log.error("文件删除失败: fileUrl={}", fileUrl, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成文件预览URL（可直接在浏览器访问）
     */
    public String generatePreviewUrl(String fileUrl, int expireMinutes, String publicKey) {
        // 查找文件资源
        FileResource fileResource = fileResourceRepository.findByFileUrl(fileUrl)
            .orElseThrow(() -> new IllegalArgumentException("文件不存在"));

        try {
            // 验证文件访问
            fileResource.validateForAccess();

            // 对于加密文件，需要验证密钥
            if (fileResource.requiresDecryption()) {
                if (StringUtils.isBlank(publicKey)) {
                    throw new IllegalArgumentException("加密文件需要提供解密密钥");
                }
                if (!encryptionService.validatePublicKey(publicKey)) {
                    throw new IllegalArgumentException("解密密钥格式错误");
                }
            }

            // 生成预签名URL
            return fileStorageService.generatePreviewUrl(fileResource, expireMinutes * 60);

        } catch (Exception e) {
            log.error("生成预览URL失败: fileUrl={}", fileUrl, e);
            throw new RuntimeException("生成预览URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证上传参数
     */
    private void validateUploadParams(MultipartFile file, String bucketName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException("存储桶名称不能为空");
        }
        if (StringUtils.isBlank(file.getOriginalFilename())) {
            throw new IllegalArgumentException("文件名不能为空");
        }
    }
}