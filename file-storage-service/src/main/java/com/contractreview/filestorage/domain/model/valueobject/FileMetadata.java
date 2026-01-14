package com.contractreview.filestorage.domain.model.valueobject;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 文件元数据值对象
 * 
 * @author ContractReview Team
 */
@Value
public class FileMetadata {
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "pdf", "doc", "docx", "txt", "jpg", "jpeg", "png"
    );

    String fileName;
    String fileType;
    Long fileSize;
    LocalDateTime createdTime;
    LocalDateTime updatedTime;

    private FileMetadata(String fileName, String fileType, Long fileSize, 
                        LocalDateTime createdTime, LocalDateTime updatedTime) {
        validateFileName(fileName);
        validateFileSize(fileSize);
        validateFileType(fileType);
        
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdTime = createdTime != null ? createdTime : LocalDateTime.now();
        this.updatedTime = updatedTime != null ? updatedTime : LocalDateTime.now();
    }

    /**
     * 创建文件元数据
     */
    public static FileMetadata of(String fileName, String fileType, Long fileSize) {
        return new FileMetadata(fileName, fileType, fileSize, null, null);
    }

    /**
     * 创建文件元数据（包含时间）
     */
    public static FileMetadata of(String fileName, String fileType, Long fileSize, 
                                 LocalDateTime createdTime, LocalDateTime updatedTime) {
        return new FileMetadata(fileName, fileType, fileSize, createdTime, updatedTime);
    }

    /**
     * 验证文件名
     */
    private void validateFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (fileName.length() > 240) {
            throw new IllegalArgumentException("文件名长度不能超过240个字符");
        }
    }

    /**
     * 验证文件大小
     */
    private void validateFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("文件大小必须大于0");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }
    }

    /**
     * 验证文件类型
     */
    private void validateFileType(String fileType) {
        if (StringUtils.isBlank(fileType)) {
            throw new IllegalArgumentException("文件类型不能为空");
        }
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension() {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 验证文件扩展名是否被允许
     */
    public boolean isAllowedExtension() {
        String extension = getFileExtension();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 更新时间
     */
    public FileMetadata updateTime() {
        return new FileMetadata(fileName, fileType, fileSize, createdTime, LocalDateTime.now());
    }
}