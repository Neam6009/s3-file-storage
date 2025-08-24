package com.awss3.filestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    private final String BUCKET_NAME = "test-bucket";
    private final String USERNAME = "test-user";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET_NAME);
    }

    @Test
    @DisplayName("Should upload file successfully")
    void uploadFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logistics_report.pdf",
                "application/pdf",
                "test data".getBytes(StandardCharsets.UTF_8)
        );
        s3Service.uploadFile(USERNAME, file);

        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        verify(s3Client, times(1)).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());

        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(capturedRequest.key()).isEqualTo("test-user/logistics_report.pdf");
        assertThat(requestBodyCaptor.getValue().contentLength()).isEqualTo(file.getSize());
    }

    @Test
    @DisplayName("Should download file successfully")
    void downloadFile_Success() {
        String filename = "logistics_report.pdf";
        byte[] expectedData = "file content".getBytes();
        String key = USERNAME + "/" + filename;

        GetObjectResponse getObjectResponse = GetObjectResponse.builder().build();
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(getObjectResponse, expectedData);

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

        byte[] actualData = s3Service.downloadFile(USERNAME, filename);

        assertThat(actualData).isEqualTo(expectedData);
        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObjectAsBytes(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo(key);
        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET_NAME);
    }

    @Test
    @DisplayName("Should throw NoSuchKeyException when downloading a non-existent file")
    void downloadFile_NotFound() {
        String filename = "non_existent_file.txt";
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.class);

        assertThrows(NoSuchKeyException.class, () -> {
            s3Service.downloadFile(USERNAME, filename);
        });
    }


    @Test
    @DisplayName("Should find files matching the search term")
    void getUserFiles_WithSearchTerm() {
        String searchTerm = "logistics";
        S3Object object1 = S3Object.builder().key("test-user/logistics_report.pdf").build();
        S3Object object2 = S3Object.builder().key("test-user/invoice_2025.pdf").build();
        S3Object object3 = S3Object.builder().key("test-user/old_logistics_data.csv").build();

        ListObjectsV2Response listResponse = ListObjectsV2Response.builder()
                .contents(object1, object2, object3)
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResponse);
        List<String> results = s3Service.getUserFiles(USERNAME, searchTerm);

        assertThat(results).hasSize(2);
        assertThat(results).containsExactlyInAnyOrder("logistics_report.pdf", "old_logistics_data.csv");
    }

    @Test
    @DisplayName("Should return all user files when search term is empty")
    void getUserFiles_WithEmptySearchTerm() {
        S3Object object1 = S3Object.builder().key("test-user/logistics_report.pdf").build();
        S3Object object2 = S3Object.builder().key("test-user/invoice_2025.pdf").build();

        ListObjectsV2Response listResponse = ListObjectsV2Response.builder()
                .contents(object1, object2)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResponse);
        List<String> results = s3Service.getUserFiles(USERNAME, "");

        assertThat(results).hasSize(2);
        assertThat(results).containsExactlyInAnyOrder("logistics_report.pdf", "invoice_2025.pdf");
    }

    @Test
    @DisplayName("Should return an empty list if no files match")
    void getUserFiles_NoMatches() {
        String searchTerm = "finance";
        S3Object object1 = S3Object.builder().key("test-user/logistics_report.pdf").build();
        ListObjectsV2Response listResponse = ListObjectsV2Response.builder().contents(object1).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResponse);
        List<String> results = s3Service.getUserFiles(USERNAME, searchTerm);

        assertThat(results).isEmpty();
    }
}