package com.rio_rishabhNEU.UserApp.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rio_rishabhNEU.UserApp.DAO.UserDAO;
import com.rio_rishabhNEU.UserApp.ExceptionHandlers.EmailNotAvailableException;
import com.rio_rishabhNEU.UserApp.ExceptionHandlers.EmailNotProvidedException;
import com.rio_rishabhNEU.UserApp.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;



@Service
public class UserService {

    @Autowired
    private UserDAO userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User createUser(User user) throws EmailNotAvailableException {
        String userEmail = user.getEmail();
        List<String> allEmails = getAllUserEmails();

        if (userEmail == null) {
            throw new EmailNotProvidedException("Email not provided");
        }

        if (allEmails.contains(userEmail)) {
            throw new EmailNotAvailableException("Email already taken: " + userEmail);
        }
        // Set UUID and timestamps
        user.setId(UUID.randomUUID());
        user.setAccountCreated(LocalDateTime.now());
        user.setAccountUpdated(LocalDateTime.now());

        // Hash the password
        String hashedPassword = hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    public List<String> getAllUserEmails() {
        return userRepository.findAllEmails();
    }

    public Optional<User> getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user;
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
