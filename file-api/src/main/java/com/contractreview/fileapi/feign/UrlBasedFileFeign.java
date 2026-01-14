package com.contractreview.fileapi.feign;

import com.contractreview.fileapi.config.FeignClientConfig;
import com.contractreview.fileapi.dto.response.FileInfoResponse;
import com.contractreview.fileapi.dto.response.FileUploadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * URL模式文件操作Feign客户端
 *
 * @author ContractReview Team
 * @version 1.0.0
 */
@FeignClient(
    name = "contract-file-storage-service",
    path = "/contract-file/api/v1/files",
    contextId = "urlBasedFileFeign",
    configuration = FeignClientConfig.class
)
public interface UrlBasedFileFeign {

    /**
     * 通过URL上传文件
     *
     * @param file 上传的文件
     * @param bucketName 存储桶名称
     * @param publicKey 公钥（可选，用于加密）
     * @param needPreview 是否需要预览（可选，默认false）
     * @return 上传响应
     */
    @PostMapping(value = "/upload-by-url", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Object> uploadByUrl(
            @RequestPart("file") MultipartFile file,
            @RequestParam("bucketName") String bucketName,
            @RequestParam(value = "publicKey", required = false) String publicKey,
            @RequestParam(value = "needPreview", defaultValue = "false") boolean needPreview
    );

    /**
     * 通过URL下载文件
     *
     * @param fileUrl 文件URL
     * @param publicKey 公钥（可选，用于解密）
     * @return 文件二进制数据
     */
    @GetMapping("/download-by-url")
    ResponseEntity<byte[]> downloadByUrl(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam(value = "publicKey", required = false) String publicKey
    );

    /**
     * 通过URL查询文件信息
     *
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    @GetMapping("/query-by-url")
    ResponseEntity<Object> queryByUrl(@RequestParam("fileUrl") String fileUrl);

    /**
     * 通过URL删除文件
     *
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    @DeleteMapping("/delete-by-url")
    ResponseEntity<Object> deleteByUrl(@RequestParam("fileUrl") String fileUrl);

    /**
     * 生成文件预览URL
     *
     * @param fileUrl 文件URL
     * @param expiryMinutes 过期时间（分钟，默认60）
     * @param publicKey 公钥（可选，用于解密）
     * @return 预览URL
     */
    @PostMapping("/preview-url")
    ResponseEntity<Object> generatePreviewUrl(
            @RequestBody String fileUrl,
            @RequestParam(value = "expiryMinutes", defaultValue = "60") int expiryMinutes,
            @RequestParam(value = "publicKey", required = false) String publicKey
    );
}