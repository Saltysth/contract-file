package com.contractreview.filestorage.application.service;

import com.contractreview.filestorage.domain.model.FileResource;
import com.contractreview.filestorage.domain.model.valueobject.AttachmentUuid;
import com.contractreview.filestorage.domain.repository.FileResourceRepository;
import com.contractreview.filestorage.domain.service.FileStorageService;
import com.contractreview.filestorage.domain.service.EncryptionService;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileInfoResponse;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UUID模式文件服务
 * 
 * @author ContractReview Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UuidBasedFileService {

    private final FileStorageService fileStorageService;
    private final FileResourceRepository fileResourceRepository;
    private final EncryptionService encryptionService;

    /**
     * 通过UUID上传文件
     */
    @Transactional
    public FileUploadResponse uploadByUuid(MultipartFile file, String bucketName, String privateKey, boolean needPreview) {
        // 验证参数
        validateUploadParameters(file, bucketName);
        
        // 验证加密密钥（如果提供）
        boolean encrypted = StringUtils.isNotBlank(privateKey);
        if (encrypted && !encryptionService.validatePublicKey(privateKey)) {
            throw new IllegalArgumentException("加密密钥格式错误");
        }

        // 创建文件资源
        FileResource fileResource = FileResource.create(
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize(),
            bucketName,
            "UUID_UPLOAD",
            encrypted
        );

        // 验证文件可以上传
        fileResource.validateForUpload();

        try {
            // 存储文件
            if (encrypted) {
                fileStorageService.storeEncryptedFile(fileResource, file, privateKey);
            } else {
                fileStorageService.storeFile(fileResource, file);
            }

            // 保存元数据
            FileResource savedResource = fileResourceRepository.save(fileResource);

            log.info("UUID文件上传成功: uuid={}, fileName={}, encrypted={}", 
                savedResource.getAttachmentUuid().getValue(), 
                savedResource.getMetadata().getFileName(),
                encrypted);

            // 根据needPreview参数决定返回预览URL还是原始URL
            String fileUrl;
            if (needPreview && !encrypted) {
                // 非加密文件可以生成预览URL
                fileUrl = fileStorageService.generatePreviewUrl(fileResource, 3600);
            } else {
                // 加密文件或不需要预览时返回文件UUID（用于后续通过UUID访问）
                fileUrl = savedResource.getAttachmentUuid().getValue();
            }

            return FileUploadResponse.builder()
                .uuid(savedResource.getAttachmentUuid().getValue())
                .fileUrl(fileUrl)
                .fileName(savedResource.getMetadata().getFileName())
                .fileSize(savedResource.getMetadata().getFileSize())
                .fileType(savedResource.getMetadata().getFileType())
                .isEncrypted(savedResource.requiresEncryption())
                .timestamp(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("UUID文件上传失败: fileName={}", file.getOriginalFilename(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过UUID下载文件
     */
    public byte[] downloadByUuid(String fileUuid, String privateKey) {
        validateUuidParameter(fileUuid);
        
        try {
            // 查询文件资源
            FileResource fileResource = fileResourceRepository.findByFileUuid(fileUuid)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + fileUuid));
            
            // 验证访问权限
            fileResource.validateForAccess();
            
            // 从MinIO下载文件
            byte[] fileContent = fileStorageService.retrieveFile(fileResource);
            
            // 解密文件内容（如果需要）
            if (fileResource.requiresDecryption()) {
                if (privateKey == null || privateKey.trim().isEmpty()) {
                    throw new IllegalArgumentException("文件已加密，需要提供私钥");
                }
                fileContent = encryptionService.decrypt(fileContent, privateKey);
            }
            
            log.info("UUID文件下载成功: uuid={}", fileUuid);
            return fileContent;
            
        } catch (Exception e) {
            log.error("UUID文件下载失败: uuid={}", fileUuid, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过UUID查询文件信息
     */
    public FileInfoResponse queryByUuid(String fileUuid) {
        validateUuidParameter(fileUuid);
        
        try {
            FileResource fileResource = fileResourceRepository.findByFileUuid(fileUuid)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + fileUuid));
            
            return FileInfoResponse.builder()
                .uuid(fileResource.getAttachmentUuid().getValue())
                .fileUrl(fileResource.getAttachmentUuid().getValue()) // UUID模式下使用UUID作为标识
                .fileName(fileResource.getMetadata().getFileName())
                .fileSize(fileResource.getMetadata().getFileSize())
                .fileType(fileResource.getMetadata().getFileType())
                .bucketName(fileResource.getStorageLocation().getBucketName())
                .directory(fileResource.getStorageLocation().getDirectory())
                .isEncrypted(fileResource.getEncryptionMetadata().getIsEncrypted())
                .createdTime(fileResource.getMetadata().getCreatedTime())
                .updatedTime(fileResource.getMetadata().getUpdatedTime())
                .build();
            
        } catch (Exception e) {
            log.error("UUID文件查询失败: uuid={}", fileUuid, e);
            throw new RuntimeException("文件查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过UUID删除文件
     */
    @Transactional
    public void deleteByUuid(String fileUuid) {
        validateUuidParameter(fileUuid);
        
        try {
            // 查询文件资源
            FileResource fileResource = fileResourceRepository.findByFileUuid(fileUuid)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + fileUuid));
            
            // 从MinIO删除文件
            fileStorageService.deleteFile(fileResource);
            
            // 从数据库删除记录
            AttachmentUuid uuid = AttachmentUuid.of(fileUuid);
            fileResourceRepository.deleteByUuid(uuid);
            
            log.info("UUID文件删除成功: uuid={}", fileUuid);
            
        } catch (Exception e) {
            log.error("UUID文件删除失败: uuid={}", fileUuid, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成UUID文件的预览URL
     */
    public String generatePreviewUrl(String fileUuid, int expiryMinutes) {
        validateUuidParameter(fileUuid);
        
        try {
            // 查询文件资源
            FileResource fileResource = fileResourceRepository.findByFileUuid(fileUuid)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + fileUuid));
            
            // 检查文件是否加密
            if (fileResource.getEncryptionMetadata().getIsEncrypted()) {
                throw new IllegalArgumentException("加密文件无法生成预览URL，请使用下载接口");
            }
            
            // 生成预签名URL
            return fileStorageService.generatePreviewUrl(fileResource, expiryMinutes);
            
        } catch (Exception e) {
            log.error("生成UUID文件预览URL失败: uuid={}", fileUuid, e);
            throw new RuntimeException("生成预览URL失败: " + e.getMessage(), e);
        }
    }

    private void validateUploadParameters(MultipartFile file, String bucketName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("存储桶名称不能为空");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
    }

    private void validateUuidParameter(String fileUuid) {
        if (fileUuid == null || fileUuid.trim().isEmpty()) {
            throw new IllegalArgumentException("文件UUID不能为空");
        }
    }
}