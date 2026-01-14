package com.contractreview.fileapi.feign;

import com.alibaba.cloud.commons.lang.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务Feign客户端 - 统一接口
 *
 * 这个类作为统一入口，委托给具体的UUID和URL模式客户端
 *
 * @author ContractReview Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FileStorageFeign {

    private final UrlBasedFileFeign urlBasedFileFeign;
    private final UuidBasedFileFeign uuidBasedFileFeign;

    // ==================== URL模式操作 ====================

    /**
     * 通过URL上传文件
     *
     * @param file 文件
     * @param bucketName 存储桶名称
     * @param publicKey 公钥（可选，用于加密）
     * @param needPreview 是否需要预览
     * @return 上传响应
     */
    public Object uploadByUrl(MultipartFile file, String bucketName, String publicKey, boolean needPreview) {
        try {
            return urlBasedFileFeign.uploadByUrl(file, bucketName, publicKey, needPreview).getBody();
        } catch (Exception e) {
            throw new RuntimeException("URL文件上传失败", e);
        }
    }

    /**
     * 通过URL下载文件
     *
     * @param fileUrl 文件URL
     * @param publicKey 公钥（可选，用于解密）
     * @return 文件二进制数据
     */
    public byte[] downloadByUrl(String fileUrl, String publicKey) {
        try {
            return urlBasedFileFeign.downloadByUrl(fileUrl, publicKey).getBody();
        } catch (Exception e) {
            throw new RuntimeException("URL文件下载失败", e);
        }
    }

    /**
     * 通过URL查询文件信息
     *
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    public Object queryByUrl(String fileUrl) {
        try {
            return urlBasedFileFeign.queryByUrl(fileUrl).getBody();
        } catch (Exception e) {
            throw new RuntimeException("URL文件查询失败", e);
        }
    }

    /**
     * 通过URL删除文件
     *
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    public Object deleteByUrl(String fileUrl) {
        try {
            return urlBasedFileFeign.deleteByUrl(fileUrl).getBody();
        } catch (Exception e) {
            throw new RuntimeException("URL文件删除失败", e);
        }
    }

    // ==================== UUID模式操作 ====================

    /**
     * 通过UUID上传文件
     *
     * @param file 文件
     * @param bucketName 存储桶名称
     * @param privateKey 私钥（可选，用于加密）
     * @param needPreview 是否需要预览
     * @return 上传响应
     */
    public Object uploadByUuid(MultipartFile file, String bucketName, String privateKey, boolean needPreview) {
        try {
            return uuidBasedFileFeign.uploadByUuid(file, bucketName, privateKey, needPreview).getBody();
        } catch (Exception e) {
            throw new RuntimeException("UUID文件上传失败", e);
        }
    }

    /**
     * 通过UUID下载文件
     *
     * @param fileUuid 文件UUID
     * @param privateKey 私钥（可选，用于解密）
     * @return 文件二进制数据
     */
    public byte[] downloadByUuid(String fileUuid, String privateKey) {
        try {
            return uuidBasedFileFeign.downloadByUuid(fileUuid, privateKey).getBody();
        } catch (Exception e) {
            throw new RuntimeException("UUID文件下载失败", e);
        }
    }

    /**
     * 通过UUID查询文件信息
     *
     * @param fileUuid  文件UUID
     * @param authToken
     * @return 文件信息
     */
    public Object queryByUuid(String fileUuid, String authToken) {
        try {
            if (StringUtils.isEmpty(authToken)) {
                return uuidBasedFileFeign.queryByUuid(fileUuid).getBody();
            } else {
                return uuidBasedFileFeign.queryByUuid(fileUuid, authToken).getBody();
            }
        } catch (Exception e) {
            throw new RuntimeException("UUID文件查询失败", e);
        }
    }

    /**
     * 通过UUID删除文件
     *
     * @param fileUuid 文件UUID
     * @return 删除结果
     */
    public Object deleteByUuid(String fileUuid) {
        try {
            return uuidBasedFileFeign.deleteByUuid(fileUuid).getBody();
        } catch (Exception e) {
            throw new RuntimeException("UUID文件删除失败", e);
        }
    }

    /**
     * 生成UUID文件预览URL
     *
     * @param fileUuid 文件UUID
     * @param expiryMinutes 过期时间（分钟）
     * @return 预览URL
     */
    public Object generatePreviewUrl(String fileUuid, int expiryMinutes) {
        try {
            return uuidBasedFileFeign.generatePreviewUrl(fileUuid, expiryMinutes).getBody();
        } catch (Exception e) {
            throw new RuntimeException("生成预览URL失败", e);
        }
    }
}