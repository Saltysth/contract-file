package com.contractreview.filestorage.infrastructure.config;

import com.contractreview.filestorage.domain.service.BucketNameValidator;
import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * MinIO配置
 * 
 * @author ContractReview Team
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinIOConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    @PostConstruct
    public void validateBucketName() {
        if (bucketName != null && !bucketName.trim().isEmpty()) {
            BucketNameValidator.ValidationResult result = BucketNameValidator.validate(bucketName);
            if (!result.isValid()) {
                throw new IllegalArgumentException("MinIO bucket名称不符合S3标准: " + result.getMessage());
            }
            // 使用标准化后的名称
            this.bucketName = result.getNormalizedName();
        }
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}