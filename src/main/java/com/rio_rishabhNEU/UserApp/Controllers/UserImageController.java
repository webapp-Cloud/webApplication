package com.rio_rishabhNEU.UserApp.Controllers;

import com.rio_rishabhNEU.UserApp.Model.UserImage;
import com.rio_rishabhNEU.UserApp.Service.ImageService;
import com.rio_rishabhNEU.UserApp.util.AuthUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/user/self")
public class UserImageController {
    private static final Logger logger = LoggerFactory.getLogger(UserImageController.class);
    private final ImageService imageService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public UserImageController(ImageService imageService, MeterRegistry meterRegistry) {
        this.imageService = imageService;
        this.meterRegistry = meterRegistry;
        logger.info("UserImageController initialized");
    }

    @PostMapping("/pic")
    public ResponseEntity<UserImage> uploadProfilePic(@RequestParam("file") MultipartFile file) {
        meterRegistry.counter("api.calls", "endpoint", "/v1/user/self/pic", "method", "POST").increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        String email = AuthUtil.getAuthenticatedUserEmail();
        try {
            logger.info("Received profile picture upload request for user: {}", email);
            if (email == null) {
                logger.warn("Unauthorized attempt to upload profile picture");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            UserImage image = imageService.uploadImage(file);
            logger.info("Successfully uploaded profile picture for user: {}, imageId: {}",
                    email, image.getId());
            sample.stop(meterRegistry.timer("api.response.time",
                    "endpoint", "/v1/user/self/pic",
                    "method", "POST"));
            return new ResponseEntity<>(image, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Failed to upload profile picture for user: {}, error: {}",
                    email, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/pic")
    public ResponseEntity<UserImage> getProfilePic() {
        meterRegistry.counter("api.calls", "endpoint", "/v1/user/self/pic", "method", "GET").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        String email = AuthUtil.getAuthenticatedUserEmail();
        logger.info("Received request to get profile picture for user: {}", email);
        if (email == null) {
            logger.warn("Unauthorized attempt to get profile picture");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserImage image = imageService.getImage();
        if (image == null) {
            logger.info("No profile picture found for user: {}", email);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.debug("Retrieved profile picture: imageId={}, fileName={}",
                image.getId(), image.getFileName());
        sample.stop(meterRegistry.timer("api.response.time",
                "endpoint", "/v1/user/self/pic",
                "method", "GET"));
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    @DeleteMapping("/pic")
    public ResponseEntity<Void> deleteProfilePic() {
        meterRegistry.counter("api.calls", "endpoint", "/v1/user/self/pic", "method", "DELETE").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        String email = AuthUtil.getAuthenticatedUserEmail();
        logger.info("Received request to delete profile picture for user: {}", email);
        if (email == null) {
            logger.warn("Unauthorized attempt to delete profile picture");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        boolean deleted = imageService.deleteImage();
        sample.stop(meterRegistry.timer("api.response.time",
                "endpoint", "/v1/user/self/pic",
                "method", "DELETE"));

        if (!deleted) {
            logger.info("No profile picture found to delete for user: {}", email);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("Successfully deleted profile picture for user: {}", email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}