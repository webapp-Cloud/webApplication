package com.rio_rishabhNEU.UserApp.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private final S3Client s3Client;
    private final String bucketName;
    private final MeterRegistry meterRegistry;

    public S3Service(@Value("${aws.s3.bucket:default-bucket-name}") String bucketName,
                     MeterRegistry meterRegistry) {
        this.bucketName = bucketName;
        this.meterRegistry = meterRegistry;
        this.s3Client = S3Client.builder().build();
    }


    public String uploadFile(MultipartFile file, UUID userId) throws IOException {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            String key = userId + "/" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            logger.info("File uploaded successfully to S3: {}", key);
            return key;
        } catch (Exception e) {
            logger.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        } finally {
            sample.stop(meterRegistry.timer("s3.upload.time"));
        }
    }

    public void deleteFile(String key) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File deleted successfully from S3: {}", key);
        } catch (Exception e) {
            logger.error("Error deleting file from S3", e);
            throw new RuntimeException("Failed to delete file from S3", e);
        } finally {
            sample.stop(meterRegistry.timer("s3.delete.time"));
        }
    }
}