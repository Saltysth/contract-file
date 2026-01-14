package com.contractreview.filestorage.infrastructure.repository.impl;

import com.contractreview.filestorage.domain.model.FileResource;
import com.contractreview.filestorage.domain.model.valueobject.*;
import com.contractreview.filestorage.domain.repository.FileResourceRepository;
import com.contractreview.filestorage.infrastructure.entity.FileEntity;
import com.contractreview.filestorage.infrastructure.repository.JpaFileResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 文件资源仓储实现
 * 
 * @author ContractReview Team
 */
@Repository
@RequiredArgsConstructor
public class FileResourceRepositoryImpl implements FileResourceRepository {

    private final JpaFileResourceRepository jpaRepository;

    @Override
    @Transactional
    public FileResource save(FileResource fileResource) {
        FileEntity entity = toEntity(fileResource);
        FileEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<FileResource> findByUuid(AttachmentUuid uuid) {
        return jpaRepository.findByAttachmentUuid(uuid.getValue())
                .map(this::toDomain);
    }

    @Override
    public Optional<FileResource> findByFileUrl(String fileUrl) {
        return jpaRepository.findByFileUrl(fileUrl)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByUuid(AttachmentUuid uuid) {
        jpaRepository.deleteByAttachmentUuid(uuid.getValue());
    }

    @Override
    @Transactional
    public void deleteByFileUrl(String fileUrl) {
        jpaRepository.deleteByFileUrl(fileUrl);
    }

    @Override
    public boolean existsByUuid(AttachmentUuid uuid) {
        return jpaRepository.existsByAttachmentUuid(uuid.getValue());
    }

    @Override
    public boolean existsByFileUrl(String fileUrl) {
        return jpaRepository.existsByFileUrl(fileUrl);
    }

    @Override
    public Optional<FileResource> findByFileUuid(String fileUuid) {
        return jpaRepository.findByAttachmentUuid(fileUuid)
                .map(this::toDomain);
    }

    /**
     * 领域对象转实体
     */
    private FileEntity toEntity(FileResource fileResource) {
        return FileEntity.builder()
                .id(fileResource.getId())
                .attachmentUuid(fileResource.getAttachmentUuid().getValue())
                .directory(fileResource.getStorageLocation().getDirectory())
                .fileUrl(fileResource.getStorageLocation().getFileUrl())
                .fileType(fileResource.getMetadata().getFileType())
                .fileName(fileResource.getMetadata().getFileName())
                .fileSize(fileResource.getMetadata().getFileSize())
                .bucketName(fileResource.getStorageLocation().getBucketName())
                .sourceType(fileResource.getSourceType())
                .isEncrypted(fileResource.getEncryptionMetadata().getIsEncrypted())
                .encryptionAlgorithm(fileResource.getEncryptionMetadata().getEncryptionAlgorithm())
                .createdTime(fileResource.getMetadata().getCreatedTime())
                .updatedTime(fileResource.getMetadata().getUpdatedTime())
                .build();
    }

    /**
     * 实体转领域对象
     */
    private FileResource toDomain(FileEntity entity) {
        AttachmentUuid uuid = AttachmentUuid.of(entity.getAttachmentUuid());
        FileMetadata metadata = FileMetadata.of(
                entity.getFileName(),
                entity.getFileType(),
                entity.getFileSize(),
                entity.getCreatedTime(),
                entity.getUpdatedTime()
        );
        StorageLocation location = StorageLocation.of(
                entity.getBucketName(),
                entity.getDirectory(),
                entity.getFileUrl()
        );
        EncryptionMetadata encryption = EncryptionMetadata.of(
                entity.getIsEncrypted(),
                entity.getEncryptionAlgorithm()
        );

        return FileResource.rebuild(
                entity.getId(),
                uuid,
                metadata,
                location,
                encryption,
                entity.getSourceType()
        );
    }
}