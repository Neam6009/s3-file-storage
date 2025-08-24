package com.awss3.filestorage.controller;

import com.awss3.filestorage.service.S3Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/{username}/upload")
    public ResponseEntity<String> upload(@PathVariable("username") String username,@RequestParam("file") MultipartFile file) throws IOException {
        s3Service.uploadFile(username,file);
        return ResponseEntity.ok("S3 File Uploaded successfully");
    }

    @GetMapping("/{username}/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable("filename") String filename, @PathVariable String username){
        byte[] fileData = s3Service.downloadFile(username,filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+filename)
                .body(fileData);
    }

    @GetMapping("/{username}/search")
    public ResponseEntity<List<String>> search(@PathVariable("username") String username,
                                               @RequestParam(name = "searchKey", required = false,defaultValue = "") String searchKey){
        List<String> searchResults = s3Service.getUserFiles(username,searchKey);
        return ResponseEntity.ok(searchResults);
    }



}
