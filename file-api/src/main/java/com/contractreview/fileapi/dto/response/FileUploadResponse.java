package com.contractreview.fileapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件上传响应DTO
 * 
 * @author ContractReview Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    
    /**
     * 文件UUID标识
     */
    private String uuid;
    
    /**
     * 文件访问URL
     */
    private String fileUrl;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件MIME类型
     */
    private String contentType;
    
    /**
     * 是否加密存储
     */
    private Boolean encrypted;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 文件MD5哈希值
     */
    private String md5Hash;
    
    /**
     * 存储路径
     */
    private String storagePath;
    
    /**
     * 业务标识
     */
    private String businessId;
}