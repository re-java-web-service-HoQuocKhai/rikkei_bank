package com.re.rikkei_bank.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadService {
    String uploadFile(MultipartFile file) throws IOException;
    void deleteFile(String publicId) throws IOException;
}
