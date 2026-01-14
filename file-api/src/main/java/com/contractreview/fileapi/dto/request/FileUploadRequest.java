package com.contractreview.fileapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 文件上传请求DTO
 * 
 * @author ContractReview Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {
    
    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;
    
    /**
     * 文件MIME类型
     */
    @NotBlank(message = "文件类型不能为空")
    private String contentType;
    
    /**
     * 是否加密存储
     */
    @Builder.Default
    private Boolean encrypted = false;
    
    /**
     * 文件描述
     */
    @Size(max = 500, message = "文件描述长度不能超过500个字符")
    private String description;
    
    /**
     * 业务标识
     */
    @Size(max = 100, message = "业务标识长度不能超过100个字符")
    private String businessId;
    
    /**
     * 文件标签
     */
    @Size(max = 200, message = "文件标签长度不能超过200个字符")
    private String tags;
}