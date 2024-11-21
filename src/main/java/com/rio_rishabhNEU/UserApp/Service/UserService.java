package com.rio_rishabhNEU.UserApp.Service;

import com.rio_rishabhNEU.UserApp.DAO.UserDAO;
import com.rio_rishabhNEU.UserApp.ExceptionHandlers.EmailNotAvailableException;
import com.rio_rishabhNEU.UserApp.ExceptionHandlers.EmailNotProvidedException;
import com.rio_rishabhNEU.UserApp.Model.User;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final UserDAO userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SNSService snsService;
    private final MeterRegistry meterRegistry;

    public UserService(UserDAO userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       SNSService snsService,
                       MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.snsService = snsService;
        this.meterRegistry = meterRegistry;
        logger.info("UserService initialized");
    }

    public User createUser(User user) throws EmailNotAvailableException {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // Validate user input
            validateNewUser(user);

            // Process and set user data
            String userEmail = user.getEmail().toLowerCase().trim();
            String verificationToken = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();

            user.setId(UUID.randomUUID());
            user.setEmail(userEmail);
            user.setPassword(hashPassword(user.getPassword()));
            user.setAccountCreated(now);
            user.setAccountUpdated(now);
            user.setEmailVerified(false);
            user.setVerificationToken(verificationToken);
            user.setTokenExpiry(now.plusMinutes(2));

            // Save user
            User savedUser = userRepository.save(user);
            logger.info("User created successfully with ID: {}", savedUser.getId());

            // Publish SNS message for email verification
            try {
                logger.debug("Publishing verification message to SNS for user: {}", userEmail);
                snsService.publishUserCreationMessage(userEmail, user.getFirstName(), verificationToken);
                logger.info("Verification message published successfully for user: {}", userEmail);
            } catch (Exception e) {
                logger.error("Failed to publish verification message to SNS for user: {}. Error: {}",
                        userEmail, e.getMessage(), e);
                // Consider if you want to delete the user or handle this differently
                // For now, we'll let the user be created but log the error
                meterRegistry.counter("sns.publish.errors").increment();
            }

            return savedUser;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            meterRegistry.counter("user.creation.errors").increment();
            throw e;
        } finally {
            sample.stop(meterRegistry.timer("service.create.user.time"));
        }
    }

    private void validateNewUser(User user) throws EmailNotAvailableException {
        // Check if email is provided
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            logger.warn("Attempt to create user with empty email");
            throw new EmailNotProvidedException("Email is required");
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            logger.warn("Invalid email format attempted: {}", user.getEmail());
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check if email is already in use
        String normalizedEmail = user.getEmail().toLowerCase().trim();
        if (getAllUserEmails().contains(normalizedEmail)) {
            logger.warn("Attempt to create user with existing email: {}", normalizedEmail);
            throw new EmailNotAvailableException("Email already taken: " + normalizedEmail);
        }

        // Validate required fields
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    @Transactional
    public boolean verifyUser(String email, String token) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Attempting to verify user with email: {}", email);
            Optional<User> userOpt = getUserByEmail(email);

            if (userOpt.isEmpty()) {
                logger.warn("Verification attempt for non-existent user: {}", email);
                return false;
            }

            User user = userOpt.get();

            if (user.isEmailVerified()) {
                logger.info("User already verified: {}", email);
                return true;
            }

            if (user.getVerificationToken() == null ||
                    !user.getVerificationToken().equals(token) ||
                    user.getTokenExpiry().isBefore(LocalDateTime.now())) {
                logger.warn("Invalid or expired verification attempt for user: {}", email);
                return false;
            }

            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setTokenExpiry(null);
            user.setAccountUpdated(LocalDateTime.now());
            updateUser(user);

            logger.info("Successfully verified user: {}", email);
            meterRegistry.counter("user.verifications.success").increment();
            return true;
        } catch (Exception e) {
            logger.error("Error during user verification for email {}: {}", email, e.getMessage(), e);
            meterRegistry.counter("user.verifications.error").increment();
            return false;
        } finally {
            sample.stop(meterRegistry.timer("service.verify.email.time"));
        }
    }

    public boolean isUserVerified(String email) {
        try {
            Optional<User> user = getUserByEmail(email);
            return user.map(User::isEmailVerified).orElse(false);
        } catch (Exception e) {
            logger.error("Error checking verification status for user {}: {}", email, e.getMessage());
            return false;
        }
    }

    public void setVerificationToken(User user, String token) {
        try {
            user.setVerificationToken(token);
            user.setTokenExpiry(LocalDateTime.now().plusMinutes(2));
            updateUser(user);
            logger.debug("New verification token set for user: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Error setting verification token for user {}: {}",
                    user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to set verification token", e);
        }
    }

    public List<String> getAllUserEmails() {
        try {
            return userRepository.findAllEmails();
        } catch (Exception e) {
            logger.error("Error retrieving all user emails: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Optional<User> getUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email.toLowerCase().trim());
        } catch (Exception e) {
            logger.error("Error retrieving user by email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public User updateUser(User user) {
        try {
            user.setAccountUpdated(LocalDateTime.now());
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error updating user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public String hashPassword(String password) {
        try {
            return passwordEncoder.encode(password);
        } catch (Exception e) {
            logger.error("Error hashing password: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public boolean validatePassword(String rawPassword, String hashedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("Error validating password: {}", e.getMessage(), e);
            return false;
        }
    }
}