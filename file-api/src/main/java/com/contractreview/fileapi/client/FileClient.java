package com.contractreview.fileapi.client;

import com.contractreview.fileapi.dto.request.FileUploadRequest;
import com.contractreview.fileapi.dto.response.FileInfoResponse;
import com.contractreview.fileapi.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * 文件存储客户端接口
 * 提供URL和UUID两种模式的文件操作
 * 
 * @author ContractReview Team
 * @version 1.0.0
 */
public interface FileClient {
    
    // ==================== URL模式接口 ====================
    
    /**
     * 通过URL上传文件
     * 
     * @param file 要上传的文件
     * @param encrypted 是否加密存储
     * @return 文件上传响应，包含访问URL
     */
    FileUploadResponse uploadByUrl(MultipartFile file, boolean encrypted);
    
    /**
     * 通过URL上传文件（异步）
     * 
     * @param file 要上传的文件
     * @param encrypted 是否加密存储
     * @return 异步文件上传响应
     */
    CompletableFuture<FileUploadResponse> uploadByUrlAsync(MultipartFile file, boolean encrypted);
    
    /**
     * 通过URL下载文件
     * 
     * @param fileUrl 文件访问URL
     * @return 文件输入流
     */
    InputStream downloadByUrl(String fileUrl);
    
    /**
     * 通过URL下载文件（异步）
     * 
     * @param fileUrl 文件访问URL
     * @return 异步文件输入流
     */
    CompletableFuture<InputStream> downloadByUrlAsync(String fileUrl);
    
    /**
     * 通过URL查询文件信息
     * 
     * @param fileUrl 文件访问URL
     * @return 文件信息
     */
    FileInfoResponse queryByUrl(String fileUrl);
    
    /**
     * 通过URL查询文件信息（异步）
     * 
     * @param fileUrl 文件访问URL
     * @return 异步文件信息
     */
    CompletableFuture<FileInfoResponse> queryByUrlAsync(String fileUrl);
    
    /**
     * 通过URL删除文件
     * 
     * @param fileUrl 文件访问URL
     * @return 删除是否成功
     */
    boolean deleteByUrl(String fileUrl);
    
    /**
     * 通过URL删除文件（异步）
     * 
     * @param fileUrl 文件访问URL
     * @return 异步删除结果
     */
    CompletableFuture<Boolean> deleteByUrlAsync(String fileUrl);
    
    // ==================== UUID模式接口 ====================
    
    /**
     * 上传文件并生成UUID标识
     * 
     * @param file 要上传的文件
     * @param encrypted 是否加密存储
     * @return 文件上传响应，包含UUID
     */
    FileUploadResponse uploadByUuid(MultipartFile file, boolean encrypted);
    
    /**
     * 上传文件并生成UUID标识（异步）
     * 
     * @param file 要上传的文件
     * @param encrypted 是否加密存储
     * @return 异步文件上传响应
     */
    CompletableFuture<FileUploadResponse> uploadByUuidAsync(MultipartFile file, boolean encrypted);
    
    /**
     * 通过UUID下载文件
     * 
     * @param uuid 文件UUID标识
     * @return 文件输入流
     */
    InputStream downloadByUuid(String uuid);
    
    /**
     * 通过UUID下载文件（异步）
     * 
     * @param uuid 文件UUID标识
     * @return 异步文件输入流
     */
    CompletableFuture<InputStream> downloadByUuidAsync(String uuid);
    
    /**
     * 通过UUID查询文件信息
     * 
     * @param uuid 文件UUID标识
     * @return 文件信息
     */
    FileInfoResponse queryByUuid(String uuid);

    FileInfoResponse queryByUuid(String uuid, String authToken);

    /**
     * 通过UUID查询文件信息（异步）
     * 
     * @param uuid 文件UUID标识
     * @return 异步文件信息
     */
    CompletableFuture<FileInfoResponse> queryByUuidAsync(String uuid);
    
    /**
     * 通过UUID删除文件
     * 
     * @param uuid 文件UUID标识
     * @return 删除是否成功
     */
    boolean deleteByUuid(String uuid);
    
    /**
     * 通过UUID删除文件（异步）
     * 
     * @param uuid 文件UUID标识
     * @return 异步删除结果
     */
    CompletableFuture<Boolean> deleteByUuidAsync(String uuid);
}