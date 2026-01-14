package com.contractreview.filestorage.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI配置类
 * 配置文件存储服务的Swagger文档
 *
 * @author ContractReview Team
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:18080}")
    private String serverPort;

    @Bean
    public OpenAPI fileStorageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Contract File Storage API")
                        .description("合同文件存储微服务 RESTful API 文档\n\n" +
                                "本服务提供基于URL和UUID的双模式文件操作功能，支持：\n" +
                                "• 文件上传与下载（AES-256加密）\n" +
                                "• 文件元数据查询与管理\n" +
                                "• MinIO分布式存储集成\n" +
                                "• 文件预览URL生成\n" +
                                "• 多桶存储管理")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Contract File Storage Team")
                                .email("support@contract-file.com")
                                .url("https://github.com/contract-review/file-storage"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/contract-file")
                                .description("开发环境服务器"),
                        new Server()
                                .url("https://api.contract-file.com")
                                .description("生产环境服务器")
                ))
                .tags(List.of(
                        new Tag()
                                .name("URL-based Operations")
                                .description("基于URL的文件操作接口"),
                        new Tag()
                                .name("UUID-based Operations")
                                .description("基于UUID的文件操作接口"),
                        new Tag()
                                .name("File Management")
                                .description("文件管理相关接口")
                ));
    }
}