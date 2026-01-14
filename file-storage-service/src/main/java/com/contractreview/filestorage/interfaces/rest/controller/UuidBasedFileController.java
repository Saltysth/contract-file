package com.contractreview.filestorage.interfaces.rest.controller;

import com.contractreview.filestorage.application.service.UuidBasedFileService;
import com.contractreview.filestorage.interfaces.rest.dto.response.ApiResponse;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileInfoResponse;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileUploadResponse;
import com.ruoyi.feign.annotation.RemotePreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * UUID模式文件操作控制器
 *
 * @author ContractReview Team
 */
@Tag(name = "UUID-based Operations", description = "基于UUID的文件操作接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/files/uuid")
@RequiredArgsConstructor
public class UuidBasedFileController {

    private final UuidBasedFileService uuidBasedFileService;

    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @Operation(
            summary = "通过UUID上传文件",
            description = "使用指定UUID上传文件到MinIO存储桶。UUID格式：{timestamp}-{randomString}。支持AES-256加密存储。"
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadByUuid(
            @Parameter(description = "要上传的文件", required = true, schema = @Schema(type = "string", format = "binary"))
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "存储桶名称", required = true, example = "contracts-bucket")
            @RequestParam("bucketName") String bucketName,
            @Parameter(description = "加密私钥（可选）", required = false)
            @RequestParam(value = "privateKey", required = false) String privateKey,
            @Parameter(description = "是否需要生成预览", required = false, example = "false")
            @RequestParam(value = "needPreview", required = false, defaultValue = "false") boolean needPreview) {
        
        try {
            FileUploadResponse response = uuidBasedFileService.uploadByUuid(file, bucketName, privateKey, needPreview);
            
            return ResponseEntity.ok(ApiResponse.success(response, "文件上传成功"));
            
        } catch (IllegalArgumentException e) {
            log.warn("UUID文件上传参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS004", e.getMessage()));
        } catch (Exception e) {
            log.error("UUID文件上传失败: fileName={}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS009", "文件上传失败"));
        }
    }

    /**
     * 通过UUID下载文件
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @GetMapping("/download")
    public ResponseEntity<?> downloadByUuid(
            @RequestParam("fileUuid") String fileUuid,
            @RequestParam(value = "privateKey", required = false) String privateKey) {
        
        try {
            byte[] fileData = uuidBasedFileService.downloadByUuid(fileUuid, privateKey);
            
            // 获取文件信息用于设置响应头
            FileInfoResponse fileInfo = uuidBasedFileService.queryByUuid(fileUuid);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileInfo.getFileType()));
            headers.setContentLength(fileData.length);

            // Fix Content-Disposition header for non-ASCII characters
            String encodedFileName = UriUtils.encode(fileInfo.getFileName(), StandardCharsets.UTF_8);
            String contentDisposition = String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s",
                encodedFileName, encodedFileName);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileData);
                
        } catch (IllegalArgumentException e) {
            log.warn("UUID文件下载参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS001", e.getMessage()));
        } catch (Exception e) {
            log.error("UUID文件下载失败: fileUuid={}", fileUuid, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS010", "文件下载失败"));
        }
    }

    /**
     * 通过UUID查询文件信息
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common,guest')")
    @GetMapping("/query")
    public ResponseEntity<ApiResponse<FileInfoResponse>> queryByUuid(@RequestParam("fileUuid") String fileUuid) {
        
        try {
            FileInfoResponse fileInfo = uuidBasedFileService.queryByUuid(fileUuid);
            
            return ResponseEntity.ok(ApiResponse.success(fileInfo));
            
        } catch (IllegalArgumentException e) {
            log.warn("UUID文件查询参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS001", e.getMessage()));
        } catch (Exception e) {
            log.error("UUID文件查询失败: fileUuid={}", fileUuid, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS001", "文件查询失败"));
        }
    }

    /**
     * 通过UUID删除文件
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteByUuid(@RequestParam("fileUuid") String fileUuid) {
        
        try {
            uuidBasedFileService.deleteByUuid(fileUuid);
            
            return ResponseEntity.ok(ApiResponse.success("文件删除成功"));
            
        } catch (IllegalArgumentException e) {
            log.warn("UUID文件删除参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS001", e.getMessage()));
        } catch (Exception e) {
            log.error("UUID文件删除失败: fileUuid={}", fileUuid, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS001", "文件删除失败"));
        }
    }

    /**
     * 生成UUID文件预览URL
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common,guest')")
    @GetMapping("/preview-url")
    public ResponseEntity<ApiResponse<String>> generatePreviewUrl(
            @RequestParam("fileUuid") String fileUuid,
            @RequestParam(value = "expiryMinutes", defaultValue = "60") int expiryMinutes) {
        
        try {
            String previewUrl = uuidBasedFileService.generatePreviewUrl(fileUuid, expiryMinutes);
            
            return ResponseEntity.ok(ApiResponse.success(previewUrl, "预览URL生成成功，可直接在浏览器中访问"));
            
        } catch (IllegalArgumentException e) {
            log.warn("生成UUID文件预览URL参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS001", e.getMessage()));
        } catch (Exception e) {
            log.error("生成UUID文件预览URL失败: fileUuid={}", fileUuid, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS001", "生成预览URL失败"));
        }
    }
}