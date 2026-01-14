package com.contractreview.filestorage.interfaces.rest.controller;

import com.contractreview.filestorage.application.service.UrlBasedFileService;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileInfoResponse;
import com.contractreview.filestorage.interfaces.rest.dto.response.FileUploadResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * URL操作控制器测试
 * 
 * @author ContractReview Team
 */
@WebMvcTest(UrlBasedFileController.class)
class UrlBasedFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlBasedFileService urlBasedFileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadByUrl_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "test content".getBytes());
        
        FileUploadResponse mockResponse = FileUploadResponse.builder()
            .uuid("20240921143022-a8b9c1d2")
            .fileUrl("/test-bucket/2024/09/21/20240921143022-a8b9c1d2/test.pdf")
            .fileName("test.pdf")
            .fileSize(12L)
            .fileType("application/pdf")
            .isEncrypted(false)
            .timestamp(LocalDateTime.now())
            .build();

        when(urlBasedFileService.uploadByUrl(any(), eq("test-bucket"), isNull(), false))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(multipart("/api/v1/files/upload-by-url")
                .file(file)
                .param("bucketName", "test-bucket"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.fileName").value("test.pdf"))
            .andExpect(jsonPath("$.data.fileSize").value(12))
            .andExpect(jsonPath("$.data.isEncrypted").value(false));
    }

    @Test
    void downloadByUrl_Success() throws Exception {
        // Given
        String fileUrl = "/test-bucket/2024/09/21/uuid/test.pdf";
        byte[] fileContent = "test content".getBytes();
        
        FileInfoResponse fileInfo = FileInfoResponse.builder()
            .fileName("test.pdf")
            .fileType("application/pdf")
            .build();

        when(urlBasedFileService.downloadByUrl(fileUrl, null))
            .thenReturn(fileContent);
        when(urlBasedFileService.queryByUrl(fileUrl))
            .thenReturn(fileInfo);

        // When & Then
        mockMvc.perform(get("/api/v1/files/download-by-url")
                .param("fileUrl", fileUrl))
            .andExpect(status().isOk())
            .andExpect(content().bytes(fileContent))
            .andExpect(header().string("Content-Type", "application/pdf"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment; filename=\"test.pdf\"")));
    }

    @Test
    void downloadByUrl_WithChineseCharacters_Success() throws Exception {
        // Given
        String fileUrl = "/test-bucket/2024/09/21/uuid/王聪-租房合同.pdf";
        byte[] fileContent = "test content".getBytes();

        FileInfoResponse fileInfo = FileInfoResponse.builder()
            .fileName("王聪-租房合同.pdf")
            .fileType("application/pdf")
            .build();

        when(urlBasedFileService.downloadByUrl(fileUrl, null))
            .thenReturn(fileContent);
        when(urlBasedFileService.queryByUrl(fileUrl))
            .thenReturn(fileInfo);

        // When & Then
        mockMvc.perform(get("/api/v1/files/download-by-url")
                .param("fileUrl", fileUrl))
            .andExpect(status().isOk())
            .andExpect(content().bytes(fileContent))
            .andExpect(header().string("Content-Type", "application/pdf"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("filename*=UTF-8''%E7%8E%8B%E8%81%AA-%E7%A7%9F%E6%88%BF%E5%90%88%E5%90%8C.pdf")))
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment; filename=")));
    }

    @Test
    void queryByUrl_Success() throws Exception {
        // Given
        String fileUrl = "/test-bucket/2024/09/21/uuid/test.pdf";
        FileInfoResponse mockResponse = FileInfoResponse.builder()
            .uuid("20240921143022-a8b9c1d2")
            .fileUrl(fileUrl)
            .fileName("test.pdf")
            .fileSize(12L)
            .fileType("application/pdf")
            .bucketName("test-bucket")
            .isEncrypted(false)
            .createdTime(LocalDateTime.now())
            .updatedTime(LocalDateTime.now())
            .build();

        when(urlBasedFileService.queryByUrl(fileUrl))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/files/query-by-url")
                .param("fileUrl", fileUrl))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.fileName").value("test.pdf"))
            .andExpect(jsonPath("$.data.fileSize").value(12))
            .andExpect(jsonPath("$.data.isEncrypted").value(false));
    }

    @Test
    void deleteByUrl_Success() throws Exception {
        // Given
        String fileUrl = "/test-bucket/2024/09/21/uuid/test.pdf";

        // When & Then
        mockMvc.perform(delete("/api/v1/files/delete-by-url")
                .param("fileUrl", fileUrl))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("文件删除成功"));
    }

    @Test
    void uploadByUrl_InvalidFile_ReturnsBadRequest() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "", "text/plain", new byte[0]);

        when(urlBasedFileService.uploadByUrl(any(), anyString(), any(), false))
            .thenThrow(new IllegalArgumentException("文件不能为空"));

        // When & Then
        mockMvc.perform(multipart("/api/file/upload-by-url")
                .file(emptyFile)
                .param("bucketName", "test-bucket"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("FS004"));
    }

    @Test
    void downloadByUrl_FileNotFound_ReturnsBadRequest() throws Exception {
        // Given
        String fileUrl = "/test-bucket/2024/09/21/uuid/nonexistent.pdf";
        
        when(urlBasedFileService.downloadByUrl(fileUrl, null))
            .thenThrow(new IllegalArgumentException("文件不存在"));

        // When & Then
        mockMvc.perform(get("/api/v1/files/download-by-url")
                .param("fileUrl", fileUrl))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("FS001"));
    }
}