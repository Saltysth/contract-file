package com.contractreview.filestorage.infrastructure.repository;

import com.contractreview.filestorage.infrastructure.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA文件资源仓储接口
 * 
 * @author ContractReview Team
 */
@Repository
public interface JpaFileResourceRepository extends JpaRepository<FileEntity, Long> {

    /**
     * 根据UUID查找文件
     */
    Optional<FileEntity> findByAttachmentUuid(String attachmentUuid);

    /**
     * 根据文件URL查找文件
     */
    Optional<FileEntity> findByFileUrl(String fileUrl);

    /**
     * 根据UUID删除文件
     */
    void deleteByAttachmentUuid(String attachmentUuid);

    /**
     * 根据文件URL删除文件
     */
    void deleteByFileUrl(String fileUrl);

    /**
     * 检查UUID是否存在
     */
    boolean existsByAttachmentUuid(String attachmentUuid);

    /**
     * 检查文件URL是否存在
     */
    boolean existsByFileUrl(String fileUrl);
}