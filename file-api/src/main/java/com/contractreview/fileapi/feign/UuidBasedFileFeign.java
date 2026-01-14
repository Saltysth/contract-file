package com.contractreview.fileapi.feign;

import com.contractreview.fileapi.config.FeignClientConfig;
import com.contractreview.fileapi.dto.response.FileInfoResponse;
import com.contractreview.fileapi.dto.response.FileUploadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * UUID模式文件操作Feign客户端
 *
 * @author ContractReview Team
 * @version 1.0.0
 */
@FeignClient(
    name = "contract-file-storage-service",
    path = "/contract-file/api/v1/files/uuid",
    contextId = "uuidBasedFileFeign",
    configuration = FeignClientConfig.class
)
public interface UuidBasedFileFeign {

    /**
     * 通过UUID上传文件
     *
     * @param file 上传的文件
     * @param bucketName 存储桶名称
     * @param privateKey 私钥（可选，用于加密）
     * @param needPreview 是否需要预览（可选，默认false）
     * @return 上传响应
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Object> uploadByUuid(
            @RequestPart("file") MultipartFile file,
            @RequestParam("bucketName") String bucketName,
            @RequestParam(value = "privateKey", required = false) String privateKey,
            @RequestParam(value = "needPreview", defaultValue = "false") boolean needPreview
    );

    /**
     * 通过UUID下载文件
     *
     * @param fileUuid 文件UUID
     * @param privateKey 私钥（可选，用于解密）
     * @return 文件二进制数据
     */
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<byte[]> downloadByUuid(
            @RequestParam("fileUuid") String fileUuid,
            @RequestParam(value = "privateKey", required = false) String privateKey
    );

    /**
     * 通过UUID查询文件信息
     *
     * @param fileUuid 文件UUID
     * @return 文件信息
     */
    @GetMapping("/query")
    ResponseEntity<Object> queryByUuid(@RequestParam("fileUuid") String fileUuid);

    @GetMapping("/query")
    ResponseEntity<Object> queryByUuid(@RequestParam("fileUuid") String fileUuid, @RequestHeader(name = "X-Internal-Auth-Secret", required = false) String authHeader);

    /**
     * 通过UUID删除文件
     *
     * @param fileUuid 文件UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    ResponseEntity<Object> deleteByUuid(@RequestParam("fileUuid") String fileUuid);

    /**
     * 生成UUID文件预览URL
     *
     * @param fileUuid 文件UUID
     * @param expiryMinutes 过期时间（分钟，默认60）
     * @return 预览URL
     */
    @GetMapping("/preview-url")
    ResponseEntity<Object> generatePreviewUrl(
            @RequestParam("fileUuid") String fileUuid,
            @RequestParam(value = "expiryMinutes", defaultValue = "60") int expiryMinutes
    );
}