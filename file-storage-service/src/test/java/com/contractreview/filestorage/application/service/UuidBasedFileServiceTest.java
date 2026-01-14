package com.contractreview.filestorage.application.service;

import com.contractreview.filestorage.domain.model.FileResource;
import com.contractreview.filestorage.domain.model.valueobject.AttachmentUuid;
import com.contractreview.filestorage.domain.repository.FileResourceRepository;
import com.contractreview.filestorage.domain.service.FileStorageService;
import com.contractreview.filestorage.domain.service.EncryptionService;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileInfoResponse;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileUploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UUID文件服务测试
 * 
 * @author ContractReview Team
 */
@ExtendWith(MockitoExtension.class)
class UuidBasedFileServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private FileResourceRepository fileResourceRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private UuidBasedFileService uuidBasedFileService;

    private MockMultipartFile testFile;
    private FileResource mockFileResource;
    private String testUuid;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Hello World".getBytes()
        );
        
        testUuid = UUID.randomUUID().toString();
        
        // 创建模拟的FileResource
        mockFileResource = mock(FileResource.class);
        AttachmentUuid attachmentUuid = mock(AttachmentUuid.class);
        when(attachmentUuid.getValue()).thenReturn(testUuid);
        when(mockFileResource.getAttachmentUuid()).thenReturn(attachmentUuid);
    }

    @Test
    void uploadByUuid_成功上传文件() throws Exception {
        // Given
        when(FileResource.create(anyString(), anyString(), anyLong(), anyString(), anyString(), anyBoolean()))
            .thenReturn(mockFileResource);
        when(fileResourceRepository.save(any(FileResource.class))).thenReturn(mockFileResource);
        
        // When
        FileUploadResponse result = uuidBasedFileService.uploadByUuid(testFile, "test-bucket", null, false);
        
        // Then
        assertNotNull(result);
        assertEquals(testUuid, result.getUuid());
        assertEquals("test.txt", result.getFileName());
        assertEquals(11L, result.getFileSize());
        assertEquals("text/plain", result.getFileType());

        verify(fileStorageService).storeFile(eq(mockFileResource), any(
            org.springframework.web.multipart.MultipartFile.class));
        verify(fileResourceRepository).save(any(FileResource.class));
    }

    @Test
    void uploadByUuid_文件为空时抛出异常() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> uuidBasedFileService.uploadByUuid(emptyFile, "test-bucket", null, false)
        );
        
        assertEquals("文件不能为空", exception.getMessage());
    }

    @Test
    void uploadByUuid_存储桶名称为空时抛出异常() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> uuidBasedFileService.uploadByUuid(testFile, "", null, false)
        );
        
        assertEquals("存储桶名称不能为空", exception.getMessage());
    }

    @Test
    void downloadByUuid_成功下载文件() throws Exception {
        // Given
        byte[] expectedContent = "Hello World".getBytes();
        when(fileResourceRepository.findByFileUuid(testUuid)).thenReturn(Optional.of(mockFileResource));
        when(fileStorageService.retrieveFile(mockFileResource)).thenReturn(expectedContent);
        when(mockFileResource.requiresDecryption()).thenReturn(false);
        
        // When
        byte[] result = uuidBasedFileService.downloadByUuid(testUuid, null);
        
        // Then
        assertArrayEquals(expectedContent, result);
        verify(mockFileResource).validateForAccess();
        verify(fileStorageService).retrieveFile(mockFileResource);
    }

    @Test
    void downloadByUuid_文件不存在时抛出异常() {
        // Given
        when(fileResourceRepository.findByFileUuid(testUuid)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> uuidBasedFileService.downloadByUuid(testUuid, null)
        );
        
        assertTrue(exception.getMessage().contains("文件下载失败"));
    }

    @Test
    void downloadByUuid_UUID格式无效时抛出异常() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> uuidBasedFileService.downloadByUuid("invalid-uuid", null)
        );
        
        assertEquals("无效的UUID格式: invalid-uuid", exception.getMessage());
    }

    @Test
    void queryByUuid_成功查询文件信息() {
        // Given
        when(fileResourceRepository.findByFileUuid(testUuid)).thenReturn(Optional.of(mockFileResource));
        
        // 模拟FileResource的各种getter方法
        when(mockFileResource.getMetadata()).thenReturn(mock(com.contractreview.filestorage.domain.model.valueobject.FileMetadata.class));
        when(mockFileResource.getStorageLocation()).thenReturn(mock(com.contractreview.filestorage.domain.model.valueobject.StorageLocation.class));
        when(mockFileResource.getEncryptionMetadata()).thenReturn(mock(com.contractreview.filestorage.domain.model.valueobject.EncryptionMetadata.class));
        
        when(mockFileResource.getMetadata().getFileName()).thenReturn("test.txt");
        when(mockFileResource.getMetadata().getFileSize()).thenReturn(11L);
        when(mockFileResource.getMetadata().getFileType()).thenReturn("text/plain");
        when(mockFileResource.getStorageLocation().getBucketName()).thenReturn("test-bucket");
        when(mockFileResource.getEncryptionMetadata().getIsEncrypted()).thenReturn(false);
        when(mockFileResource.getMetadata().getCreatedTime()).thenReturn(java.time.LocalDateTime.now());
        
        // When
        FileInfoResponse result = uuidBasedFileService.queryByUuid(testUuid);
        
        // Then
        assertNotNull(result);
        assertEquals(testUuid, result.getUuid());
        assertEquals("test.txt", result.getFileName());
        assertEquals(11L, result.getFileSize());
        assertEquals("text/plain", result.getFileType());
        assertEquals("test-bucket", result.getBucketName());
        assertEquals(false, result.getIsEncrypted());
        assertNotNull(result.getUpdatedTime());
    }

    @Test
    void deleteByUuid_成功删除文件() {
        // Given
        when(fileResourceRepository.findByFileUuid(testUuid)).thenReturn(Optional.of(mockFileResource));
        
        // When
        assertDoesNotThrow(() -> uuidBasedFileService.deleteByUuid(testUuid));
        
        // Then
        verify(fileStorageService).deleteFile(mockFileResource);
        verify(fileResourceRepository).deleteByUuid(any(AttachmentUuid.class));
    }

    @Test
    void generatePreviewUrl_成功生成预览URL() {
        // Given
        String expectedUrl = "https://minio.example.com/test-bucket/file.txt?expires=3600";
        when(fileResourceRepository.findByFileUuid(testUuid)).thenReturn(Optional.of(mockFileResource));
        when(mockFileResource.getEncryptionMetadata()).thenReturn(mock(com.contractreview.filestorage.domain.model.valueobject.EncryptionMetadata.class));
        when(mockFileResource.getEncryptionMetadata().getIsEncrypted()).thenReturn(false);
        when(fileStorageService.generatePreviewUrl(mockFileResource, 60)).thenReturn(expectedUrl);
        
        // When
        String result = uuidBasedFileService.generatePreviewUrl(testUuid, 60);
        
        // Then
        assertEquals(expectedUrl, result);
        verify(fileStorageService).generatePreviewUrl(mockFileResource, 60);
    }

    @Test
    void generatePreviewUrl_加密文件时抛出异常() {
        // Given
        when(fileResourceRepository.findByFileUuid(testUuid)).thenReturn(Optional.of(mockFileResource));
        when(mockFileResource.getEncryptionMetadata()).thenReturn(mock(com.contractreview.filestorage.domain.model.valueobject.EncryptionMetadata.class));
        when(mockFileResource.getEncryptionMetadata().getIsEncrypted()).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> uuidBasedFileService.generatePreviewUrl(testUuid, 60)
        );
        
        assertTrue(exception.getMessage().contains("生成预览URL失败"));
    }
}