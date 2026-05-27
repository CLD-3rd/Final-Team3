package com.matchFit.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final AmazonS3 amazonS3;
    private final String bucketName;

    public S3Service(AmazonS3 amazonS3, @Value("${aws.s3.bucket}") String bucketName) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
    }

    public String uploadFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        int dotIdx = originalFileName.lastIndexOf('.');
        String extension = (dotIdx >= 0 && dotIdx < originalFileName.length() - 1)
                ? originalFileName.substring(dotIdx + 1)
                : "";
        String extSuffix = !extension.isBlank() ? "." + extension : "";
        String fileName = "sports-posts/" + UUID.randomUUID() + "_" + System.currentTimeMillis() + extSuffix;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    file.getInputStream(),
                    metadata
            );

            amazonS3.putObject(putObjectRequest);
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException | RuntimeException ex) {
            log.error("S3 파일 업로드 실패", ex);
            throw new RuntimeException("S3 파일 업로드에 실패했습니다.", ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(bucketName, fileName);
            log.info("S3 파일 삭제 완료: {}", fileName);
        } catch (Exception ex) {
            log.error("S3 파일 삭제 실패", ex);
            throw new RuntimeException("S3 파일 삭제에 실패했습니다.", ex);
        }
    }

    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        String fileName = extractFileNameFromUrl(fileUrl);
        if (fileName != null) {
            deleteFile(fileName);
        }
    }

    public String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return null;

        String bucketUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/";
        if (fileUrl.startsWith(bucketUrl)) {
            return fileUrl.substring(bucketUrl.length());
        }
        return null;
    }

    public boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) return false;

        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public void deleteByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        String key = extractKeyFromUrl(imageUrl);
        if (key == null || key.isBlank()) return;

        amazonS3.deleteObject(bucketName, key);
    }

    private String extractKeyFromUrl(String imageUrl) {
        String marker = ".amazonaws.com/";
        int idx = imageUrl.indexOf(marker);
        if (idx != -1) {
            return imageUrl.substring(idx + marker.length());
        }
        return imageUrl;
    }
}
