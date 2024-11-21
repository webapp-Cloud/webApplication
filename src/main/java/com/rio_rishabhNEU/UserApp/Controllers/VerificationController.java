package com.rio_rishabhNEU.UserApp.Controllers;

import com.rio_rishabhNEU.UserApp.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/v1/verifyEmail")
public class VerificationController {
    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);
    private final UserService userService;

    public VerificationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Void> verifyEmail(
            @RequestParam("email") String email,
            @RequestParam("token") String token) {
        logger.info("Received verification request for email: {}", email);

        boolean verified = userService.verifyUser(email, token);
        if (verified) {
            logger.info("Successfully verified email: {}", email);
            return ResponseEntity.ok().build();
        } else {
            logger.warn("Failed to verify email: {}", email);
            return ResponseEntity.badRequest().build();
        }
    }
}