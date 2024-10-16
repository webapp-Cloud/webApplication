package com.rio_rishabhNEU.UserApp;

import com.rio_rishabhNEU.UserApp.ExceptionHandlers.EmailNotAvailableException;
import com.rio_rishabhNEU.UserApp.Model.User;
import com.rio_rishabhNEU.UserApp.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerIntegrationInTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() throws EmailNotAvailableException {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create a test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser = userService.createUser(testUser);
    }

    @Test
    void testCreateUser() throws Exception {
        HashMap<String, String> newUser = new HashMap<>();
        newUser.put("email", "new@example.com");
        newUser.put("password", "newpassword");
        newUser.put("first_name", "New");
        newUser.put("last_name", "User");

        mockMvc.perform(post("/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.first_name").value("New"))
                .andExpect(jsonPath("$.last_name").value("User"))
                .andExpect(jsonPath("$.account_created").exists())
                .andExpect(jsonPath("$.account_updated").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(get("/v1/user/self")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((testUser.getEmail() + ":password").getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.first_name").value(testUser.getFirstName()))
                .andExpect(jsonPath("$.last_name").value(testUser.getLastName()))
                .andExpect(jsonPath("$.account_created").exists())
                .andExpect(jsonPath("$.account_updated").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testUpdateUser() throws Exception {
        User userUpdate = new User();
        userUpdate.setFirstName("UpdatedFirst");
        userUpdate.setLastName("UpdatedLast");
        userUpdate.setPassword("newpassword");

        mockMvc.perform(put("/v1/user/self")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((testUser.getEmail() + ":password").getBytes()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdate)))
                .andExpect(status().isNoContent());

        // Verify the update
        mockMvc.perform(get("/v1/user/self")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((testUser.getEmail() + ":password").getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("UpdatedFirst"))
                .andExpect(jsonPath("$.last_name").value("UpdatedLast"))
                .andExpect(jsonPath("$.account_updated").exists());
    }

    @Test
    void testUpdateUser_BadRequest() throws Exception {
        User userUpdate = new User();
        userUpdate.setEmail("newemail@example.com"); // This should not be allowed

        mockMvc.perform(put("/v1/user/self")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((testUser.getEmail() + ":password").getBytes()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/user/self")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(("wrong@email.com:wrongpassword").getBytes())))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    void testHeadRequest() throws Exception {
//        mockMvc.perform(head("/v1/user/self"))
//                .andExpect(status().isMethodNotAllowed());
//    }
//
//    @Test
//    void testOptionsRequest() throws Exception {
//        mockMvc.perform(options("/v1/user/self"))
//                .andExpect(status().isMethodNotAllowed());
//    }
}
