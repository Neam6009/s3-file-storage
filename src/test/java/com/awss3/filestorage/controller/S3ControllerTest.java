package com.awss3.filestorage.controller;

import com.awss3.filestorage.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(S3Controller.class)
class S3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3Service s3Service;

    private final String USERNAME = "sandy";

    @Test
    @DisplayName("POST /{username}/upload - Should return 200 OK on successful file upload")
    void upload_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello world".getBytes()
        );
        doNothing().when(s3Service).uploadFile(eq(USERNAME), any(MockMultipartFile.class));

        mockMvc.perform(multipart("/api/s3/{username}/upload", USERNAME).file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("S3 File Uploaded successfully"));
    }

    @Test
    @DisplayName("GET /{username}/download/{filename} - Should return file content with 200 OK")
    void download_Success() throws Exception {
        String filename = "report.pdf";
        byte[] fileData = "dummy pdf content".getBytes();
        when(s3Service.downloadFile(USERNAME, filename)).thenReturn(fileData);

        mockMvc.perform(get("/api/s3/{username}/download/{filename}", USERNAME, filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename))
                .andExpect(content().bytes(fileData));
    }

    @Test
    @DisplayName("GET /{username}/search?searchKey=... - Should return list of matching filenames")
    void search_WithSearchKey_ReturnsMatchingFiles() throws Exception {
        String searchKey = "logistics";
        List<String> searchResults = List.of("logistics_report.pdf", "old_logistics_data.csv");
        when(s3Service.getUserFiles(USERNAME, searchKey)).thenReturn(searchResults);

        mockMvc.perform(get("/api/s3/{username}/search", USERNAME)
                        .param("searchKey", searchKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0]").value("logistics_report.pdf"))
                .andExpect(jsonPath("$[1]").value("old_logistics_data.csv"));
    }

    @Test
    @DisplayName("GET /{username}/search - Should return all files when searchKey is absent")
    void search_WithoutSearchKey_ReturnsAllFiles() throws Exception {
        List<String> allFiles = List.of("report.pdf", "invoice.docx");
        when(s3Service.getUserFiles(USERNAME, "")).thenReturn(allFiles);

        mockMvc.perform(get("/api/s3/{username}/search", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0]").value("report.pdf"));
    }

    @Test
    @DisplayName("GET /{username}/search - Should return empty list when no files are found")
    void search_NoResults() throws Exception {
        when(s3Service.getUserFiles(USERNAME, "nonexistent")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/s3/{username}/search", USERNAME)
                        .param("searchKey", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

}