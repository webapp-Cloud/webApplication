package com.rio_rishabhNEU.UserApp.Controllers;

import com.rio_rishabhNEU.UserApp.Model.UserImage;
import com.rio_rishabhNEU.UserApp.Service.ImageService;
import com.rio_rishabhNEU.UserApp.util.AuthUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/user/self")
public class UserImageController {
    private final ImageService imageService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public UserImageController(ImageService imageService, MeterRegistry meterRegistry) {
        this.imageService = imageService;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/pic")
    public ResponseEntity<UserImage> uploadProfilePic(@RequestParam("file") MultipartFile file) {
        meterRegistry.counter("api.calls", "endpoint", "/v1/user/self/pic", "method", "POST").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            String email = AuthUtil.getAuthenticatedUserEmail();
            if (email == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            UserImage image = imageService.uploadImage(file);
            sample.stop(meterRegistry.timer("api.response.time",
                    "endpoint", "/v1/user/self/pic",
                    "method", "POST"));
            return new ResponseEntity<>(image, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/pic")
    public ResponseEntity<UserImage> getProfilePic() {
        meterRegistry.counter("api.calls", "endpoint", "/v1/user/self/pic", "method", "GET").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        String email = AuthUtil.getAuthenticatedUserEmail();
        if (email == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserImage image = imageService.getImage();
        if (image == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

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
        if (email == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        boolean deleted = imageService.deleteImage();
        sample.stop(meterRegistry.timer("api.response.time",
                "endpoint", "/v1/user/self/pic",
                "method", "DELETE"));

        if (!deleted) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}