package com.rio_rishabhNEU.UserApp.Service;

import com.rio_rishabhNEU.UserApp.DAO.UserImageDAO;
import com.rio_rishabhNEU.UserApp.Model.UserImage;
import com.rio_rishabhNEU.UserApp.util.AuthUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("png", "jpg", "jpeg"));

    private final UserImageDAO imageDAO;
    private final S3Service s3Service;
    private final MeterRegistry meterRegistry;
    private final UserService userService;

    public ImageService(UserImageDAO imageDAO,
                        S3Service s3Service,
                        MeterRegistry meterRegistry,
                        UserService userService) {
        this.imageDAO = imageDAO;
        this.s3Service = s3Service;
        this.meterRegistry = meterRegistry;
        this.userService = userService;
        logger.info("ImageService initialized");
    }

    public UserImage uploadImage(MultipartFile file) throws Exception {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new IllegalArgumentException("Invalid file format. Allowed formats: PNG, JPG, JPEG");
            }

            String email = AuthUtil.getAuthenticatedUserEmail();
            UUID userId = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();

            Optional<UserImage> existingImage = imageDAO.findByUserId(userId);
            existingImage.ifPresent(image -> {
                s3Service.deleteFile(image.getUrl());
                imageDAO.delete(image);
            });

            String s3Key = s3Service.uploadFile(file, userId);

            UserImage userImage = new UserImage();
            userImage.setFileName(originalFilename);
            userImage.setUrl(s3Key);
            userImage.setUploadDate(LocalDateTime.now());
            userImage.setUserId(userId);

            Timer.Sample dbSample = Timer.start(meterRegistry);
            UserImage savedImage = imageDAO.save(userImage);
            dbSample.stop(meterRegistry.timer("db.save.image.time"));

            logger.info("Successfully uploaded image for user: {}", email);
            return savedImage;
        } finally {
            sample.stop(meterRegistry.timer("service.upload.image.time"));
        }
    }

    public UserImage getImage() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            String email = AuthUtil.getAuthenticatedUserEmail();
            UUID userId = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();

            Timer.Sample dbSample = Timer.start(meterRegistry);
            Optional<UserImage> image = imageDAO.findByUserId(userId);
            dbSample.stop(meterRegistry.timer("db.get.image.time"));

            return image.orElse(null);
        } finally {
            sample.stop(meterRegistry.timer("service.get.image.time"));
        }
    }

    public boolean deleteImage() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            String email = AuthUtil.getAuthenticatedUserEmail();
            UUID userId = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();

            Timer.Sample dbSample = Timer.start(meterRegistry);
            Optional<UserImage> image = imageDAO.findByUserId(userId);
            dbSample.stop(meterRegistry.timer("db.get.image.time"));

            if (image.isPresent()) {
                s3Service.deleteFile(image.get().getUrl());
                imageDAO.delete(image.get());
                logger.info("Successfully deleted image for user: {}", email);
                return true;
            }
            return false;
        } finally {
            sample.stop(meterRegistry.timer("service.delete.image.time"));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}