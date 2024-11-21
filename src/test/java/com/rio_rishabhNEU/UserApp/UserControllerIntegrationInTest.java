//package com.rio_rishabhNEU.UserApp;
//
//import com.rio_rishabhNEU.UserApp.DAO.UserDAO;
//import com.rio_rishabhNEU.UserApp.Model.User;
//import com.rio_rishabhNEU.UserApp.Service.S3Service;
//import com.rio_rishabhNEU.UserApp.config.TestConfig;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//public class UserControllerIntegrationInTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private UserDAO userDAO;
//
//    @MockBean
//    private S3Service s3Service;
//
//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;
//
//    private static final String TEST_EMAIL = "test@example.com";
//    private static final String TEST_PASSWORD = "password123";
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        userDAO.deleteAll();
//
//        testUser = new User();
//        testUser.setId(UUID.randomUUID());
//        testUser.setFirstName("Test");
//        testUser.setLastName("User");
//        testUser.setEmail(TEST_EMAIL);
//        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
//        testUser.setAccountCreated(LocalDateTime.now());
//        testUser.setAccountUpdated(LocalDateTime.now());
//
//        userDAO.save(testUser);
//    }
//
//    @Test
//    public void testGetUser() throws Exception {
//        mockMvc.perform(get("/v1/user/self")
//                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
//    }
//
//    @Test
//    public void testGetUser_Unauthorized() throws Exception {
//        mockMvc.perform(get("/v1/user/self")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    public void testUpdateUser() throws Exception {
//        User updateRequest = new User();
//        updateRequest.setFirstName("UpdatedFirst");
//        updateRequest.setLastName("UpdatedLast");
//        updateRequest.setPassword("newpassword123");
//
//        mockMvc.perform(put("/v1/user/self")
//                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    public void testUpdateUser_BadRequest() throws Exception {
//        User updateRequest = new User();
//        updateRequest.setEmail("newemail@example.com"); // Should not be allowed to update
//
//        mockMvc.perform(put("/v1/user/self")
//                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void testUpdateUser_Success() throws Exception {
//        User updateRequest = new User();
//        updateRequest.setFirstName("UpdatedFirst");
//        updateRequest.setLastName("UpdatedLast");
//
//        mockMvc.perform(put("/v1/user/self")
//                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isNoContent());
//    }
//}