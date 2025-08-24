package com.awss3.filestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${cloud.aws.bucket.name}")
    private String bucketName;

    public void uploadFile(String username,MultipartFile file) throws IOException {
        String userFileName = username+"/"+file.getOriginalFilename();
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(userFileName).build(),
                RequestBody.fromBytes(file.getBytes()));
    }

    public byte[] downloadFile(String username, String filename){
        String key = username+"/"+filename;
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client
                .getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucketName).key(key).build());

        return objectAsBytes.asByteArray();
    }

    public List<String> getUserFiles(String username,String searchField){
        String userPrefix = username+"/";

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName).prefix(userPrefix).build();

        return s3Client.listObjectsV2(request).contents().stream()
                .map(S3Object::key)
                .filter(key -> key.substring(userPrefix.length()).toLowerCase().contains(searchField.toLowerCase()))
                .map(key -> key.substring(userPrefix.length()))
                .collect(Collectors.toList());
    }

}
