package com.rio_rishabhNEU.UserApp.DAO;

import com.rio_rishabhNEU.UserApp.Model.User;
import com.rio_rishabhNEU.UserApp.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.rio_rishabhNEU.UserApp.SimulationConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDAO extends JpaRepository<User, UUID> {


    @Query("SELECT u.email FROM User u")
    List<String> findAllEmails();

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT 1" , nativeQuery = true)
    Integer pingDatabase();
    default boolean isDatabaseConnected() {
        if(SimulationConfig.isSimulateDbDisconnection()){
            return false;
        }
        try{
            Integer result = pingDatabase();
            return result != null && result == 1 ;
        }
        catch (Exception e) {
            return false;
        }
    }
}
