package com.rio_rishabhNEU.UserApp;

import com.rio_rishabhNEU.UserApp.Controllers.UserController;
import com.rio_rishabhNEU.UserApp.Model.User;
import com.rio_rishabhNEU.UserApp.Service.S3Service;
import com.rio_rishabhNEU.UserApp.Service.UserService;
import com.rio_rishabhNEU.UserApp.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
public class UserControllerInTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");
    }

//    @Test
//    void testCreateUser_Success() throws Exception {
//        when(userService.createUser(any(User.class))).thenReturn(testUser);
//
//        mockMvc.perform(post("/v1/user")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"email\":\"test@example.com\",\"firstName\":\"Test\",\"lastName\":\"User\",\"password\":\"password\"}"))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.email").value("test@example.com"));
//    }

    // Add more test methods here...


}