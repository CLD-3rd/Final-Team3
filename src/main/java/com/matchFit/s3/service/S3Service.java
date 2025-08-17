package com.matchFit.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName; // matchfit-bucket

    public String uploadFile(MultipartFile file) throws IOException {
        // 파일명 생성: UUID + 타임스탬프 + 확장자
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = "sports-posts/" + UUID.randomUUID() + "_" + System.currentTimeMillis() + extension;

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            // S3에 파일 업로드 (퍼블릭 읽기 권한)
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName,
                fileName,
                file.getInputStream(),
                metadata
            );

            amazonS3.putObject(putObjectRequest);

            // 업로드된 파일의 URL 반환
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패: ", e);
            throw new RuntimeException("S3 파일 업로드에 실패했습니다.", e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(bucketName, fileName);
            log.info("S3 파일 삭제 완료: {}", fileName);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: ", e);
            throw new RuntimeException("S3 파일 삭제에 실패했습니다.", e);
        }
    }

    // URL로 파일 삭제
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        String fileName = extractFileNameFromUrl(fileUrl);
        if (fileName != null) {
            deleteFile(fileName);
        }
    }

    // URL에서 파일명 추출
    public String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        String bucketUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/";
        if (fileUrl.startsWith(bucketUrl)) {
            return fileUrl.substring(bucketUrl.length());
        }
        
        return null;
    }

    // 파일 유효성 검사
    public boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    // 안전한 삭제 구현
    public void deleteByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        String key = extractKeyFromUrl(imageUrl);
        if (key == null || key.isBlank()) return;

        // aws sdk v1 방식 (간단히 bucket + key)
        amazonS3.deleteObject(bucketName, key);
    }
    
    private String extractKeyFromUrl(String imageUrl) {
        // 일반적인 S3 퍼블릭 URL 패턴에서 key 추출
        String marker = ".amazonaws.com/";
        int idx = imageUrl.indexOf(marker);
        if (idx != -1) {
            return imageUrl.substring(idx + marker.length());
        }
        // 혹시 imageUrl이 이미 key라면 그대로 반환
        return imageUrl;
    }
    
}