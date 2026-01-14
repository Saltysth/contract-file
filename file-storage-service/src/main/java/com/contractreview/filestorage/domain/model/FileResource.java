package com.contractreview.filestorage.domain.model;

import com.contractreview.filestorage.domain.model.valueobject.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件资源聚合根
 * 
 * @author ContractReview Team
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileResource {

    private Long id;
    private AttachmentUuid attachmentUuid;
    private FileMetadata metadata;
    private StorageLocation storageLocation;
    private EncryptionMetadata encryptionMetadata;
    private String sourceType;

    /**
     * 创建新的文件资源
     */
    public static FileResource create(String fileName, String fileType, Long fileSize, 
                                    String bucketName, String sourceType, boolean encrypted) {
        AttachmentUuid uuid = AttachmentUuid.generate();
        FileMetadata metadata = FileMetadata.of(fileName, fileType, fileSize);
        StorageLocation location = StorageLocation.generateFromUuid(bucketName, uuid, fileName);
        EncryptionMetadata encryption = encrypted ? EncryptionMetadata.encrypted() : EncryptionMetadata.unencrypted();
        
        return new FileResource(null, uuid, metadata, location, encryption, sourceType);
    }

    /**
     * 从现有数据重建文件资源
     */
    public static FileResource rebuild(Long id, AttachmentUuid uuid, FileMetadata metadata, 
                                     StorageLocation location, EncryptionMetadata encryption, String sourceType) {
        return new FileResource(id, uuid, metadata, location, encryption, sourceType);
    }

    /**
     * 验证文件是否可以上传
     */
    public void validateForUpload() {
        if (!metadata.isAllowedExtension()) {
            throw new IllegalArgumentException("不支持的文件格式：" + metadata.getFileExtension());
        }
    }

    /**
     * 验证文件是否可以访问
     */
    public void validateForAccess() {
        if (attachmentUuid == null) {
            throw new IllegalStateException("文件UUID不能为空");
        }
        if (storageLocation == null) {
            throw new IllegalStateException("存储位置不能为空");
        }
    }

    /**
     * 检查是否需要加密
     */
    public boolean requiresEncryption() {
        return encryptionMetadata.getIsEncrypted();
    }

    /**
     * 检查是否需要解密
     */
    public boolean requiresDecryption() {
        return encryptionMetadata.getIsEncrypted();
    }

    /**
     * 获取MinIO对象键
     */
    public String getObjectKey() {
        return storageLocation.getObjectKey(metadata.getFileName());
    }

    /**
     * 获取文件URL
     */
    public String getFileUrl() {
        return storageLocation.getFileUrl();
    }

    /**
     * 更新元数据时间
     */
    public FileResource updateMetadata() {
        FileMetadata updatedMetadata = metadata.updateTime();
        return new FileResource(id, attachmentUuid, updatedMetadata, storageLocation, encryptionMetadata, sourceType);
    }

    /**
     * 设置ID（用于持久化后）
     */
    public FileResource withId(Long id) {
        return new FileResource(id, attachmentUuid, metadata, storageLocation, encryptionMetadata, sourceType);
    }
}