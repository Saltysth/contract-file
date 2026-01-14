package com.contractreview.filestorage.domain.model.valueobject;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 存储位置值对象
 * 
 * @author ContractReview Team
 */
@Value
public class StorageLocation {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    String bucketName;
    String directory;
    String fileUrl;

    private StorageLocation(String bucketName, String directory, String fileUrl) {
        validateBucketName(bucketName);
        this.bucketName = bucketName;
        this.directory = directory;
        this.fileUrl = fileUrl;
    }

    /**
     * 创建存储位置
     */
    public static StorageLocation of(String bucketName, String directory, String fileUrl) {
        return new StorageLocation(bucketName, directory, fileUrl);
    }

    /**
     * 根据UUID生成存储位置
     */
    public static StorageLocation generateFromUuid(String bucketName, AttachmentUuid uuid, String fileName) {
        LocalDateTime timestamp = uuid.getTimestamp();
        String datePath = timestamp.format(DATE_FORMATTER);
        String directory = datePath + "/" + uuid.getValue();
        String fileUrl = generateFileUrl(bucketName, directory, fileName);
        
        return new StorageLocation(bucketName, directory, fileUrl);
    }

    /**
     * 生成文件URL
     */
    private static String generateFileUrl(String bucketName, String directory, String fileName) {
        return String.format("/%s/%s/%s", bucketName, directory, fileName);
    }

    /**
     * 验证存储桶名称
     */
    private void validateBucketName(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException("存储桶名称不能为空");
        }
        if (bucketName.length() > 60) {
            throw new IllegalArgumentException("存储桶名称长度不能超过60个字符");
        }
    }

    /**
     * 获取完整的存储路径
     */
    public String getFullPath() {
        return bucketName + "/" + directory;
    }

    /**
     * 获取MinIO对象键
     */
    public String getObjectKey(String fileName) {
        return directory + "/" + fileName;
    }
}