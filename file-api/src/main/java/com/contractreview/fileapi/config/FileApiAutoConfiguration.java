package com.contractreview.fileapi.config;

import com.contractreview.fileapi.client.FileClient;
import com.contractreview.fileapi.client.impl.FileClientImpl;
import com.contractreview.fileapi.feign.FileStorageFeign;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * File API 自动配置类
 *
 * @author ContractReview Team
 * @version 1.0.0
 */
@Configuration
@EnableFeignClients(basePackages = "com.contractreview.fileapi.feign")
public class FileApiAutoConfiguration {

    /**
     * 创建FileClient Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public FileClient fileClient(FileStorageFeign fileStorageFeign, ObjectMapper objectMapper) {
        return new FileClientImpl(fileStorageFeign, objectMapper);
    }
}