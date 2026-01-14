package com.contractreview.filestorage.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文件实体类 - 对应数据库表
 * 
 * @author ContractReview Team
 */
@Entity
@Table(name = "file")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attachment_uuid", nullable = false, unique = true, length = 50)
    private String attachmentUuid;

    @Column(name = "directory", length = 400)
    private String directory;

    @Column(name = "file_url", length = 120)
    private String fileUrl;

    @Column(name = "file_type", length = 240)
    private String fileType;

    @Column(name = "file_name", length = 240)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "bucket_name", length = 60)
    private String bucketName;

    @Column(name = "source_type", length = 60)
    private String sourceType;

    @Column(name = "is_encrypted")
    @Builder.Default
    private Boolean isEncrypted = false;

    @Column(name = "encryption_algorithm", length = 20)
    private String encryptionAlgorithm;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}