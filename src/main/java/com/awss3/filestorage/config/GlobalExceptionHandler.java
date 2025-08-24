package com.awss3.filestorage.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NoSuchKeyException.class})
    public ResponseEntity<String> handleNoSuchKeyException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("404 - File Not Found");
    }

    @ExceptionHandler({MultipartException.class})
    public ResponseEntity<String> handleMultipartException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("400 - Invalid File Upload, Bad Request");
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleDefaultException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong, please try again later");
    }
}
