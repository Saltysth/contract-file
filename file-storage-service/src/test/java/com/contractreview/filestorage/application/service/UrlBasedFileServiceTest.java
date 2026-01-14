package com.contractreview.filestorage.application.service;

import com.contractreview.filestorage.domain.model.FileResource;
import com.contractreview.filestorage.domain.repository.FileResourceRepository;
import com.contractreview.filestorage.domain.service.EncryptionService;
import com.contractreview.filestorage.domain.service.FileStorageService;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileUploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * URL操作应用服务测试
 * 
 * @author ContractReview Team
 */
@ExtendWith(MockitoExtension.class)
class UrlBasedFileServiceTest {

    @Mock
    private FileResourceRepository fileResourceRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private UrlBasedFileService urlBasedFileService;

    private MockMultipartFile testFile;
    private FileResource testFileResource;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "test content".getBytes()
        );

        testFileResource = FileResource.create(
            "test.pdf",
            "application/pdf",
            12L,
            "test-bucket",
            "URL_UPLOAD",
            false
        );
    }

    @Test
    void uploadByUrl_Success() {
        // Given
        when(fileResourceRepository.save(any(FileResource.class)))
            .thenReturn(testFileResource.withId(1L));

        // When
        FileUploadResponse response = urlBasedFileService.uploadByUrl(
            testFile, "test-bucket", null, false);

        // Then
        assertNotNull(response);
        assertEquals("test.pdf", response.getFileName());
        assertEquals(12L, response.getFileSize());
        assertEquals("application/pdf", response.getFileType());
        assertFalse(response.getIsEncrypted());

        verify(fileStorageService).storeFile(any(FileResource.class), eq(testFile));
        verify(fileResourceRepository).save(any(FileResource.class));
    }

    @Test
    void uploadByUrl_WithEncryption_Success() {
        // Given
        String publicKey = "SGVsbG9Xb3JsZEhlbGxvV29ybGRIZWxsb1dvcmxkSGVsbG9Xb3JsZA==";
        when(encryptionService.validatePublicKey(publicKey)).thenReturn(true);
        when(fileResourceRepository.save(any(FileResource.class)))
            .thenReturn(testFileResource.withId(1L));

        // When
        FileUploadResponse response = urlBasedFileService.uploadByUrl(
            testFile, "test-bucket", publicKey, false);

        // Then
        assertNotNull(response);
        verify(encryptionService).validatePublicKey(publicKey);
        verify(fileStorageService).storeEncryptedFile(any(FileResource.class), eq(testFile), eq(publicKey));
    }

    @Test
    void uploadByUrl_EmptyFile_ThrowsException() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            urlBasedFileService.uploadByUrl(emptyFile, "test-bucket", null, false);
        });
    }

    @Test
    void downloadByUrl_Success() {
        // Given
        String fileUrl = "/test-bucket/2024/09/21/uuid/test.pdf";
        byte[] expectedData = "test content".getBytes();
        
        when(fileResourceRepository.findByFileUrl(fileUrl))
            .thenReturn(Optional.of(testFileResource));
        when(fileStorageService.retrieveFile(testFileResource))
            .thenReturn(expectedData);

        // When
        byte[] result = urlBasedFileService.downloadByUrl(fileUrl, null);

        // Then
        assertArrayEquals(expectedData, result);
        verify(fileResourceRepository).findByFileUrl(fileUrl);
        verify(fileStorageService).retrieveFile(testFileResource);
    }

    @Test
    void downloadByUrl_FileNotFound_ThrowsException() {
        // Given
        String fileUrl = "/test-bucket/2024/09/21/uuid/nonexistent.pdf";
        when(fileResourceRepository.findByFileUrl(fileUrl))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            urlBasedFileService.downloadByUrl(fileUrl, null);
        });
    }

    @Test
    void queryByUrl_Success() {
        // Given
        String fileUrl = "/test-bucket/2024/09/21/uuid/test.pdf";
        when(fileResourceRepository.findByFileUrl(fileUrl))
            .thenReturn(Optional.of(testFileResource));

        // When
        var response = urlBasedFileService.queryByUrl(fileUrl);

        // Then
        assertNotNull(response);
        assertEquals("test.pdf", response.getFileName());
        assertEquals("application/pdf", response.getFileType());
        assertEquals(12L, response.getFileSize());
    }

    @Test
    void deleteByUrl_Success() {
        // Given
        String fileUrl = "/test-bucket/2024/09/21/uuid/test.pdf";
        when(fileResourceRepository.findByFileUrl(fileUrl))
            .thenReturn(Optional.of(testFileResource));

        // When
        urlBasedFileService.deleteByUrl(fileUrl);

        // Then
        verify(fileStorageService).deleteFile(testFileResource);
        verify(fileResourceRepository).deleteByFileUrl(fileUrl);
    }
}