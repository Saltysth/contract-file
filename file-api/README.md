# File Storage API Client SDK

## 概述

File Storage API Client SDK 是一个用于文件存储服务的客户端SDK，提供了简洁易用的API接口，支持文件的上传、下载、查询和删除操作。

## 功能特性

- **双模式操作**：支持URL和UUID两种文件访问方式
- **完整CRUD**：提供文件上传、下载、查询、删除四大核心功能
- **加密支持**：支持AES加密文件存储
- **自动配置**：基于Spring Boot自动配置，开箱即用
- **Feign集成**：基于OpenFeign实现微服务调用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.saltyfish</groupId>
    <artifactId>file-api</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 配置Nacos服务发现

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:18080
        namespace: dev
        group: DEFAULT_GROUP
```

### 3. 使用FileClient

```java
@RestController
public class FileController {
    
    @Autowired
    private FileClient fileClient;
    
    // URL模式 - 文件上传
    @PostMapping("/upload")
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file) {
        return fileClient.uploadByUrl(file, false); // false表示不加密
    }
    
    // URL模式 - 文件下载
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("url") String fileUrl) {
        byte[] fileData = fileClient.downloadByUrl(fileUrl);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=file")
            .body(fileData);
    }
    
    // URL模式 - 文件查询
    @GetMapping("/info")
    public FileInfoResponse getFileInfo(@RequestParam("url") String fileUrl) {
        return fileClient.queryByUrl(fileUrl);
    }
    
    // URL模式 - 文件删除
    @DeleteMapping("/delete")
    public boolean deleteFile(@RequestParam("url") String fileUrl) {
        return fileClient.deleteByUrl(fileUrl);
    }
    
    // UUID模式 - 文件上传
    @PostMapping("/upload-uuid")
    public FileUploadResponse uploadFileWithUuid(@RequestParam("file") MultipartFile file) {
        return fileClient.uploadByUuid(file, true); // true表示加密存储
    }
    
    // UUID模式 - 文件下载
    @GetMapping("/download-uuid/{uuid}")
    public ResponseEntity<byte[]> downloadFileByUuid(@PathVariable String uuid) {
        byte[] fileData = fileClient.downloadByUuid(uuid);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=file")
            .body(fileData);
    }
}
```

## API接口说明

### URL模式接口

| 方法 | 描述 | 参数 | 返回值 |
|------|------|------|--------|
| `uploadByUrl(MultipartFile, boolean)` | 通过URL上传文件 | 文件对象, 是否加密 | FileUploadResponse |
| `downloadByUrl(String)` | 通过URL下载文件 | 文件URL | byte[] |
| `queryByUrl(String)` | 查询URL对应的文件信息 | 文件URL | FileInfoResponse |
| `deleteByUrl(String)` | 通过URL删除文件 | 文件URL | boolean |

### UUID模式接口

| 方法 | 描述 | 参数 | 返回值 |
|------|------|------|--------|
| `uploadByUuid(MultipartFile, boolean)` | 上传文件并生成UUID | 文件对象, 是否加密 | FileUploadResponse |
| `downloadByUuid(String)` | 通过UUID下载文件 | 文件UUID | byte[] |
| `queryByUuid(String)` | 查询UUID对应的文件信息 | 文件UUID | FileInfoResponse |
| `deleteByUuid(String)` | 通过UUID删除文件 | 文件UUID | boolean |

## 响应对象

### FileUploadResponse

```java
{
    "success": true,
    "message": "文件上传成功",
    "fileUrl": "http://example.com/files/abc123",
    "fileUuid": "550e8400-e29b-41d4-a716-446655440000",
    "fileName": "document.pdf",
    "fileSize": 1024000,
    "contentType": "application/pdf"
}
```

### FileInfoResponse

```java
{
    "success": true,
    "message": "查询成功",
    "fileUrl": "http://example.com/files/abc123",
    "fileUuid": "550e8400-e29b-41d4-a716-446655440000",
    "fileName": "document.pdf",
    "fileSize": 1024000,
    "contentType": "application/pdf",
    "uploadTime": "2023-12-01T10:30:00",
    "encrypted": false
}
```

## 异常处理

SDK提供了以下异常类型：

- `FileStorageException`：通用文件存储异常
- `FileNotFoundException`：文件未找到异常
- `FileUploadException`：文件上传异常

```java
try {
    FileUploadResponse response = fileClient.uploadByUrl(file, false);
} catch (FileUploadException e) {
    log.error("文件上传失败: {}", e.getMessage());
} catch (FileStorageException e) {
    log.error("文件存储异常: {}", e.getMessage());
}
```

## 配置说明

### 服务发现配置

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:18080  # Nacos服务器地址
        namespace: dev                # 命名空间
        group: DEFAULT_GROUP          # 服务分组
```

### Feign配置

```yaml
feign:
  client:
    config:
      file-storage-service:
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: basic
```

## 版本信息

- 当前版本：0.0.1
- 最低Java版本：17
- Spring Boot版本：3.2.0
- Spring Cloud版本：2023.0.0

## 技术支持

如有问题，请联系开发团队或查看项目文档。