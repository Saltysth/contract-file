package com.contractreview.filestorage.domain.repository;

import com.contractreview.filestorage.domain.model.FileResource;
import com.contractreview.filestorage.domain.model.valueobject.AttachmentUuid;

import java.util.Optional;

/**
 * 文件资源仓储接口
 * 
 * @author ContractReview Team
 */
public interface FileResourceRepository {

    /**
     * 保存文件资源
     */
    FileResource save(FileResource fileResource);

    /**
     * 根据UUID查找文件
     */
    Optional<FileResource> findByUuid(AttachmentUuid uuid);

    /**
     * 根据文件URL查找文件
     */
    Optional<FileResource> findByFileUrl(String fileUrl);

    /**
     * 根据UUID删除文件
     */
    void deleteByUuid(AttachmentUuid uuid);

    /**
     * 根据文件URL删除文件
     */
    void deleteByFileUrl(String fileUrl);

    /**
     * 检查UUID是否存在
     */
    boolean existsByUuid(AttachmentUuid uuid);

    /**
     * 检查文件URL是否存在
     */
    boolean existsByFileUrl(String fileUrl);

    /**
     * 根据文件UUID字符串查找文件
     */
    Optional<FileResource> findByFileUuid(String fileUuid);
}