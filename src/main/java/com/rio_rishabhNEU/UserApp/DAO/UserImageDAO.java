// File: src/main/java/com/rio_rishabhNEU/UserApp/DAO/UserImageDAO.java
package com.rio_rishabhNEU.UserApp.DAO;

import com.rio_rishabhNEU.UserApp.Model.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserImageDAO extends JpaRepository<UserImage, UUID> {
    Optional<UserImage> findByUserId(UUID userId);
}