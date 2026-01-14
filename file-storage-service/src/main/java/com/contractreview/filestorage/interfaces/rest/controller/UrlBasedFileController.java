package com.contractreview.filestorage.interfaces.rest.controller;

import com.contractreview.filestorage.application.service.UrlBasedFileService;
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
 * URL操作控制器
 *
 * @author ContractReview Team
 */
@Tag(name = "URL-based Operations", description = "基于URL的文件操作接口")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class UrlBasedFileController {

    private final UrlBasedFileService urlBasedFileService;

    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @Operation(
            summary = "通过URL上传文件",
            description = "上传文件到MinIO存储桶并返回访问URL。支持AES-256加密存储。"
    )
    @PostMapping(value = "/upload-by-url", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadByUrl(
            @Parameter(description = "要上传的文件", required = true, schema = @Schema(type = "string", format = "binary"))
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "存储桶名称", required = true, example = "contracts-bucket")
            @RequestParam("bucketName") String bucketName,
            @Parameter(description = "加密公钥（可选）", required = false)
            @RequestParam(value = "publicKey", required = false) String publicKey,
            @Parameter(description = "是否需要生成预览", required = false, example = "false")
            @RequestParam(value = "needPreview", required = false, defaultValue = "false") boolean needPreview) {
        
        try {
            FileUploadResponse response = urlBasedFileService.uploadByUrl(file, bucketName, publicKey, needPreview);
            return ResponseEntity.ok(ApiResponse.success(response, "文件上传成功"));
        } catch (IllegalArgumentException e) {
            log.warn("文件上传参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS004", e.getMessage()));
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS009", "文件上传失败"));
        }
    }

    /**
     * 通过URL下载文件
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @GetMapping("/download-by-url")
    public ResponseEntity<?> downloadByUrl(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam(value = "publicKey", required = false) String publicKey) {
        
        try {
            byte[] fileData = urlBasedFileService.downloadByUrl(fileUrl, publicKey);
            
            // 获取文件信息用于设置响应头
            FileInfoResponse fileInfo = urlBasedFileService.queryByUrl(fileUrl);
            
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
            log.warn("文件下载参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS001", e.getMessage()));
        } catch (Exception e) {
            log.error("文件下载失败: fileUrl={}", fileUrl, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS010", "文件下载失败"));
        }
    }

    /**
     * 通过URL查询文件信息
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common,guest')")
    @GetMapping("/query-by-url")
    public ResponseEntity<ApiResponse<FileInfoResponse>> queryByUrl(
            @RequestParam("fileUrl") String fileUrl) {
        
        try {
            FileInfoResponse response = urlBasedFileService.queryByUrl(fileUrl);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("文件查询参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS001", e.getMessage()));
        } catch (Exception e) {
            log.error("文件查询失败: fileUrl={}", fileUrl, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS001", "文件查询失败"));
        }
    }

    /**
     * 通过URL删除文件
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @DeleteMapping("/delete-by-url")
    public ResponseEntity<ApiResponse<Void>> deleteByUrl(
            @RequestParam("fileUrl") String fileUrl) {
        
        try {
            urlBasedFileService.deleteByUrl(fileUrl);
            return ResponseEntity.ok(ApiResponse.success(null, "文件删除成功"));
        } catch (IllegalArgumentException e) {
            log.warn("文件删除参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS001", e.getMessage()));
        } catch (Exception e) {
            log.error("文件删除失败: fileUrl={}", fileUrl, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS001", "文件删除失败"));
        }
    }

    /**
     * 生成文件预览URL
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common,guest')")
    @PostMapping("/preview-url")
    public ResponseEntity<?> generatePreviewUrl(
            @RequestBody String fileUrl,
            @RequestParam(value = "expiryMinutes", defaultValue = "60") int expiryMinutes,
        @RequestParam(value = "publicKey", required = false) String publicKey) {
        try {
            String previewUrl = urlBasedFileService.generatePreviewUrl(fileUrl, expiryMinutes, publicKey);
            
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                "previewUrl", previewUrl,
                "expiryMinutes", String.valueOf(expiryMinutes),
                "message", "预览URL生成成功，可直接在浏览器中访问"
            )));
            
        } catch (IllegalArgumentException e) {
            log.warn("生成预览URL参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("FS001", e.getMessage()));
        } catch (Exception e) {
            log.error("生成预览URL失败: fileUrl={}", fileUrl, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FS001", "生成预览URL失败"));
        }
    }
}