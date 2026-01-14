package com.contractreview.fileapi.client.impl;

import com.contractreview.fileapi.client.FileClient;
import com.contractreview.fileapi.dto.response.FileInfoResponse;
import com.contractreview.fileapi.dto.response.FileUploadResponse;
import com.contractreview.fileapi.feign.FileStorageFeign;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 文件存储客户端实现类
 *
 * @author ContractReview Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileClientImpl implements FileClient {

    private final FileStorageFeign fileStorageFeign;
    private final ObjectMapper objectMapper;

    // 默认存储桶名称
    private static final String DEFAULT_BUCKET_NAME = "default-bucket";

    // 默认加密密钥（实际使用时应该从配置或外部获取）
    private static final String DEFAULT_ENCRYPTION_KEY = "SGVsbG9Xb3JsZEhlbGxvV29ybGRIZWxsb1dvcmxkSGVsbG9Xb3JsZA==";

    // ==================== URL模式接口实现 ====================

    @Override
    public FileUploadResponse uploadByUrl(MultipartFile file, boolean encrypted) {
        log.info("开始上传文件: {}, 加密: {}", file.getOriginalFilename(), encrypted);
        try {
            Object response = fileStorageFeign.uploadByUrl(
                file,
                DEFAULT_BUCKET_NAME,
                encrypted ? DEFAULT_ENCRYPTION_KEY : null,
                false
            );
            log.info("文件上传成功: {}", response != null ? "success" : "failed");
            return parseUploadResponse(response);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<FileUploadResponse> uploadByUrlAsync(MultipartFile file, boolean encrypted) {
        return CompletableFuture.supplyAsync(() -> uploadByUrl(file, encrypted));
    }
    
    @Override
    public InputStream downloadByUrl(String fileUrl) {
        log.info("开始下载文件: {}", fileUrl);
        try {
            byte[] fileData = fileStorageFeign.downloadByUrl(fileUrl, DEFAULT_ENCRYPTION_KEY);
            if (fileData == null) {
                throw new RuntimeException("文件下载失败: 响应数据为空");
            }
            log.info("文件下载成功: {} bytes", fileData.length);
            return new ByteArrayInputStream(fileData);
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<InputStream> downloadByUrlAsync(String fileUrl) {
        return CompletableFuture.supplyAsync(() -> downloadByUrl(fileUrl));
    }
    
    @Override
    public FileInfoResponse queryByUrl(String fileUrl) {
        log.info("查询文件信息: {}", fileUrl);
        try {
            Object response = fileStorageFeign.queryByUrl(fileUrl);
            FileInfoResponse result = parseFileInfoResponse(response);
            log.info("文件信息查询成功: {}", result != null ? result.getFileName() : "null");
            return result;
        } catch (Exception e) {
            log.error("文件信息查询失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件信息查询失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<FileInfoResponse> queryByUrlAsync(String fileUrl) {
        return CompletableFuture.supplyAsync(() -> queryByUrl(fileUrl));
    }
    
    @Override
    public boolean deleteByUrl(String fileUrl) {
        log.info("删除文件: {}", fileUrl);
        try {
            Object response = fileStorageFeign.deleteByUrl(fileUrl);
            boolean success = parseBooleanResponse(response);
            log.info("文件删除{}: {}", success ? "成功" : "失败", fileUrl);
            return success;
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<Boolean> deleteByUrlAsync(String fileUrl) {
        return CompletableFuture.supplyAsync(() -> deleteByUrl(fileUrl));
    }
    
    // ==================== UUID模式接口实现 ====================
    
    @Override
    public FileUploadResponse uploadByUuid(MultipartFile file, boolean encrypted) {
        log.info("通过UUID上传文件: {}, 加密: {}", file.getOriginalFilename(), encrypted);
        try {
            Object response = fileStorageFeign.uploadByUuid(
                file,
                DEFAULT_BUCKET_NAME,
                encrypted ? DEFAULT_ENCRYPTION_KEY : null,
                false
            );
            log.info("UUID文件上传成功: {}", response != null ? "success" : "failed");
            return parseUploadResponse(response);
        } catch (Exception e) {
            log.error("UUID文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("UUID文件上传失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<FileUploadResponse> uploadByUuidAsync(MultipartFile file, boolean encrypted) {
        return CompletableFuture.supplyAsync(() -> uploadByUuid(file, encrypted));
    }
    
    @Override
    public InputStream downloadByUuid(String uuid) {
        log.info("通过UUID下载文件: {}", uuid);
        try {
            byte[] fileData = fileStorageFeign.downloadByUuid(uuid, DEFAULT_ENCRYPTION_KEY);
            if (fileData == null) {
                throw new RuntimeException("文件下载失败: 响应数据为空");
            }
            log.info("文件下载成功: {} bytes", fileData.length);
            return new ByteArrayInputStream(fileData);
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<InputStream> downloadByUuidAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> downloadByUuid(uuid));
    }
    
    @Override
    public FileInfoResponse queryByUuid(String uuid) {
        return getFileInfoResponse(uuid, null);
    }

    @Override
    public FileInfoResponse queryByUuid(String uuid, String authToken) {
        return getFileInfoResponse(uuid, authToken);
    }

    private FileInfoResponse getFileInfoResponse(String uuid, String authToken) {
        log.info("通过UUID查询文件信息: {}", uuid);
        try {
            Object response = fileStorageFeign.queryByUuid(uuid, authToken);
            FileInfoResponse result = parseFileInfoResponse(response);
            log.info("文件信息查询成功: {}", result != null ? result.getFileName() : "null");
            return result;
        } catch (Exception e) {
            log.error("文件信息查询失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件信息查询失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Async
    public CompletableFuture<FileInfoResponse> queryByUuidAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> queryByUuid(uuid));
    }
    
    @Override
    public boolean deleteByUuid(String uuid) {
        log.info("通过UUID删除文件: {}", uuid);
        try {
            Object response = fileStorageFeign.deleteByUuid(uuid);
            boolean success = parseBooleanResponse(response);
            log.info("文件删除{}: {}", success ? "成功" : "失败", uuid);
            return success;
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<Boolean> deleteByUuidAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> deleteByUuid(uuid));
    }

    // ==================== 辅助解析方法 ====================

    /**
     * 解析文件上传响应
     */
    private FileUploadResponse parseUploadResponse(Object response) {
        if (response == null) {
            return null;
        }

        if (response instanceof FileUploadResponse) {
            return (FileUploadResponse) response;
        }

        // 处理LinkedHashMap类型（Feign常见情况）
        if (response instanceof LinkedHashMap) {
            try {
                // 检查是否是ApiResponse格式
                Map<String, Object> responseMap = (LinkedHashMap<String, Object>) response;
                if (responseMap.containsKey("success") && responseMap.containsKey("data")) {
                    Boolean success = (Boolean) responseMap.get("success");
                    if (success && responseMap.get("data") != null) {
                        // 转换data字段为FileUploadResponse
                        Object data = responseMap.get("data");
                        if (data instanceof LinkedHashMap) {
                            return objectMapper.convertValue(data, FileUploadResponse.class);
                        } else if (data instanceof FileUploadResponse) {
                            return (FileUploadResponse) data;
                        }
                    } else if (!success) {
                        String message = (String) responseMap.get("message");
                        log.error("API响应失败: {}", message);
                        throw new RuntimeException(message != null ? message : "API调用失败");
                    }
                } else {
                    // 直接将LinkedHashMap转换为FileUploadResponse
                    return objectMapper.convertValue(response, FileUploadResponse.class);
                }
            } catch (Exception e) {
                log.error("解析上传响应时发生异常: {}", e.getMessage(), e);
                throw new RuntimeException("响应解析失败: " + e.getMessage(), e);
            }
        }

        log.warn("解析上传响应时遇到未处理的类型: {}", response.getClass());
        return null;
    }

    
    /**
     * 解析文件信息响应
     */
    private FileInfoResponse parseFileInfoResponse(Object response) {
        if (response == null) {
            return null;
        }

        if (response instanceof FileInfoResponse) {
            return (FileInfoResponse) response;
        }

        // 处理LinkedHashMap类型
        if (response instanceof LinkedHashMap) {
            try {
                Map<String, Object> responseMap = (LinkedHashMap<String, Object>) response;
                if (responseMap.containsKey("success") && responseMap.containsKey("data")) {
                    Boolean success = (Boolean) responseMap.get("success");
                    if (success && responseMap.get("data") != null) {
                        // 转换data字段为FileInfoResponse
                        Object data = responseMap.get("data");
                        if (data instanceof LinkedHashMap) {
                            return objectMapper.convertValue(data, FileInfoResponse.class);
                        } else if (data instanceof FileInfoResponse) {
                            return (FileInfoResponse) data;
                        }
                    } else if (!success) {
                        String message = (String) responseMap.get("message");
                        log.error("API响应失败: {}", message);
                        throw new RuntimeException(message != null ? message : "API调用失败");
                    }
                } else {
                    // 直接将LinkedHashMap转换为FileInfoResponse
                    return objectMapper.convertValue(response, FileInfoResponse.class);
                }
            } catch (Exception e) {
                log.error("解析文件信息响应时发生异常: {}", e.getMessage(), e);
                throw new RuntimeException("响应解析失败: " + e.getMessage(), e);
            }
        }

        log.warn("解析文件信息响应时遇到未处理的类型: {}", response.getClass());
        return null;
    }

    /**
     * 解析布尔响应
     */
    private boolean parseBooleanResponse(Object response) {
        if (response == null) {
            return false;
        }

        if (response instanceof Boolean) {
            return (Boolean) response;
        }

        // 处理LinkedHashMap类型
        if (response instanceof LinkedHashMap) {
            try {
                Map<String, Object> responseMap = (LinkedHashMap<String, Object>) response;
                if (responseMap.containsKey("success") && responseMap.containsKey("data")) {
                    Boolean success = (Boolean) responseMap.get("success");
                    if (success) {
                        Object data = responseMap.get("data");
                        if (data instanceof Boolean) {
                            return (Boolean) data;
                        } else if (data instanceof String && "true".equalsIgnoreCase((String) data)) {
                            return true;
                        }
                        return data != null;
                    } else {
                        String message = (String) responseMap.get("message");
                        log.error("API响应失败: {}", message);
                        throw new RuntimeException(message != null ? message : "API调用失败");
                    }
                } else {
                    // 直接检查是否为布尔值或字符串
                    if (response instanceof Boolean) {
                        return (Boolean) response;
                    } else if (response instanceof String) {
                        return "true".equalsIgnoreCase((String) response);
                    }
                }
            } catch (Exception e) {
                log.error("解析布尔响应时发生异常: {}", e.getMessage(), e);
                throw new RuntimeException("响应解析失败: " + e.getMessage(), e);
            }
        }

        log.warn("解析布尔响应时遇到未处理的类型: {}", response.getClass());
        return false;
    }
}