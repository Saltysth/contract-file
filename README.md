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

## 环境配置

### 必需的环境变量

在启动服务前，需要配置以下环境变量或修改配置文件中的默认值：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `DB_URL` | PostgreSQL 数据库连接地址 | `jdbc:postgresql://localhost:5432/postgres` |
| `DB_USERNAME` | 数据库用户名 | `postgres` |
| `DB_PASSWORD` | 数据库密码 | **需配置** |
| `MINIO_ENDPOINT` | MinIO 服务地址 | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` | MinIO 访问密钥 | **需配置** |
| `MINIO_SECRET_KEY` | MinIO 秘密密钥 | **需配置** |
| `NACOS_SERVER` | Nacos 服务器地址 | `localhost:18848` |
| `NACOS_USERNAME` | Nacos 用户名 | `nacos` |
| `NACOS_PASSWORD` | Nacos 密码 | **需配置** |
| `RUOYI_SECRET` | 若依远程认证密钥 | **需配置** |

### 配置方式

**方式一：环境变量（推荐）**

```bash
export DB_PASSWORD=your_database_password
export MINIO_ACCESS_KEY=your_minio_access_key
export MINIO_SECRET_KEY=your_minio_secret_key
export NACOS_PASSWORD=your_nacos_password
export RUOYI_SECRET=your_ruoyi_secret_key

mvn spring-boot:run
```

**方式二：修改配置文件**

编辑 `file-storage-service/src/main/resources/application.yml`，将所有 `change_me` 替换为实际值：

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:change_me}  # 替换为实际密码

minio:
  access-key: ${MINIO_ACCESS_KEY:change_me}  # 替换为实际密钥
  secret-key: ${MINIO_SECRET_KEY:change_me}  # 替换为实际密钥

ruoyi:
  remote-auth:
    secret: ${RUOYI_SECRET:change_me}  # 替换为实际密钥
```

编辑 `file-storage-service/src/main/resources/bootstrap.yml`：

```yaml
spring:
  cloud:
    nacos:
      password: ${NACOS_PASSWORD:change_me}  # 替换为实际密码
```

### 依赖服务启动顺序

1. **PostgreSQL** (端口 5432) - 创建数据库
2. **MinIO** (端口 9000) - 创建存储桶
3. **Nacos** (端口 18848) - 配置命名空间和分组
4. **本服务** (端口 10000) - 启动文件存储服务

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
