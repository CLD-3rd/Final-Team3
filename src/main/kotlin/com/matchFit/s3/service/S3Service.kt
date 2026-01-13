package com.matchFit.s3.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID


@Service
class S3Service(
    private val amazonS3: AmazonS3,
    @Value("\${aws.s3.bucket}") private val bucketName: String
) {
    private val log = LoggerFactory.getLogger(S3Service::class.java)

    fun uploadFile(file: MultipartFile): String {
        val originalFileName = file.originalFilename ?: throw IllegalArgumentException("파일명이 없습니다.")
        val extension = originalFileName.substringAfterLast('.', "")
        val extSuffix = if (extension.isNotBlank()) ".${extension}" else ""
        val fileName = "sports-posts/${UUID.randomUUID()}_${System.currentTimeMillis()}${extSuffix}"

        val metadata = ObjectMetadata().apply {
            contentType = file.contentType
            contentLength = file.size
        }

        return try {
            val putObjectRequest = PutObjectRequest(
                bucketName,
                fileName,
                file.inputStream,
                metadata
            )

            amazonS3.putObject(putObjectRequest)
            amazonS3.getUrl(bucketName, fileName).toString()
        } catch (ex: Exception) {
            log.error("S3 파일 업로드 실패", ex)
            throw RuntimeException("S3 파일 업로드에 실패했습니다.", ex)
        }
    }

    fun deleteFile(fileName: String) {
        try {
            amazonS3.deleteObject(bucketName, fileName)
            log.info("S3 파일 삭제 완료: {}", fileName)
        } catch (ex: Exception) {
            log.error("S3 파일 삭제 실패", ex)
            throw RuntimeException("S3 파일 삭제에 실패했습니다.", ex)
        }
    }

    fun deleteFileByUrl(fileUrl: String?) {
        if (fileUrl.isNullOrBlank()) {
            return
        }

        val fileName = extractFileNameFromUrl(fileUrl)
        if (fileName != null) {
            deleteFile(fileName)
        }
    }

    fun extractFileNameFromUrl(fileUrl: String?): String? {
        if (fileUrl.isNullOrBlank()) {
            return null
        }

        val bucketUrl = "https://${bucketName}.s3.ap-northeast-2.amazonaws.com/"
        if (fileUrl.startsWith(bucketUrl)) {
            return fileUrl.substring(bucketUrl.length)
        }

        return null
    }

    fun isValidImageFile(file: MultipartFile): Boolean {
        if (file.isEmpty) {
            return false
        }

        val contentType = file.contentType
        return contentType != null && contentType.startsWith("image/")
    }

    fun deleteByUrl(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) return

        val key = extractKeyFromUrl(imageUrl)
        if (key.isNullOrBlank()) return

        amazonS3.deleteObject(bucketName, key)
    }

    private fun extractKeyFromUrl(imageUrl: String): String {
        val marker = ".amazonaws.com/"
        val idx = imageUrl.indexOf(marker)
        return if (idx != -1) {
            imageUrl.substring(idx + marker.length)
        } else {
            imageUrl
        }
    }
}
