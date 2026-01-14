# 文件存储微服务

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-brightgreen)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

</div>

## 项目简介

基于 Java 17 和 Spring Boot 构建的分布式文件存储微服务，遵循领域驱动设计（DDD）架构。提供 URL 和 UUID 双模式文件操作，支持 AES-256 加密，采用 MinIO 分布式存储后端。

系统采用多模块 Maven 结构，包含独立的客户端 SDK 和服务实现，可轻松集成到现有的 Spring Cloud 项目中。

## 核心特性

- **双模式操作**：URL 模式和 UUID 模式文件访问
- **AES-256 加密**：可选的文件内容加密保护
- **MinIO 存储**：S3 兼容的对象存储后端
- **Nacos 集成**：服务发现与配置管理
- **OpenFeign 客户端**：服务间通信支持

## 技术栈

| 技术 | 说明 |
|------|------|
| Java 17 | 开发语言 |
| Spring Boot 3.5.0 | 应用框架 |
| Spring Cloud 2025.0.0 | 微服务框架 |
| PostgreSQL | 元数据存储 |
| MinIO | 文件存储 |
| Nacos 3.1 | 服务发现与配置中心 |

## 快速开始

```bash
# 构建项目
mvn clean install

# 运行服务
cd file-storage-service
mvn spring-boot:run
```

访问 Swagger 文档：`http://localhost:18080/contract-file/swagger-ui.html`

## 使用示例

```xml
<dependency>
    <groupId>com.saltyfish</groupId>
    <artifactId>file-api</artifactId>
    <version>0.0.1</version>
</dependency>
```

```java
@Autowired
private FileClient fileClient;

// URL 模式上传
FileUploadResponse response = fileClient.uploadByUrl(file, false);

// UUID 模式上传（加密）
FileUploadResponse response = fileClient.uploadByUuid(file, true);
```

## 架构设计

```
contract-file/
├── file-api/              # 客户端 SDK 模块
└── file-storage-service/  # 主微服务（DDD 四层架构）
    ├── interfaces/        # REST 控制器
    ├── application/       # 服务编排
    ├── domain/            # 核心业务逻辑
    └── infrastructure/    # 外部集成
```

## 许可证

Apache License 2.0
