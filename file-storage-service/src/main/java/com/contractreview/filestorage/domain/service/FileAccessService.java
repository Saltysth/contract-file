package com.contractreview.filestorage.domain.service;

import com.contractreview.filestorage.domain.model.FileResource;
import com.contractreview.filestorage.domain.model.valueobject.AttachmentUuid;

import java.util.Optional;

/**
 * 文件访问领域服务
 * 
 * @author ContractReview Team
 */
public interface FileAccessService {

    /**
     * 根据UUID获取文件资源
     */
    Optional<FileResource> getFileByUuid(AttachmentUuid uuid);

    /**
     * 根据文件URL获取文件资源
     */
    Optional<FileResource> getFileByUrl(String fileUrl);

    /**
     * 验证文件访问权限
     */
    boolean hasAccessPermission(FileResource fileResource, String userId);

    /**
     * 生成临时访问URL
     */
    String generateTemporaryUrl(FileResource fileResource, int expirationMinutes);
}