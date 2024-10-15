package com.rio_rishabhNEU.UserApp;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rio_rishabhNEU.UserApp.Controllers.UserController;
import com.rio_rishabhNEU.UserApp.ExceptionHandlers.EmailNotAvailableException;
import com.rio_rishabhNEU.UserApp.Model.User;
import com.rio_rishabhNEU.UserApp.Service.UserService;
import com.rio_rishabhNEU.UserApp.util.AuthUtil;
import jakarta.persistence.GeneratedValue;
import org.hibernate.generator.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
//@WebMvcTest(UserController.class)
public class UserControllerInTest {

    @Autowired
    private MockMvc mockMvcTest;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.fromString("23e4567-e89b-12d3-a456-426614174000"));
        user.setFirstName("Rio");
        user.setLastName("Janerio");
        user.setPassword("P@s5w0rd");
        user.setEmail("rio@gmail.com");
        user.setAccountCreated(LocalDateTime.now());
        user.setAccountUpdated(LocalDateTime.now());
    }


    @Test
    void testGetUser_NotFound()throws Exception{
        try (MockedStatic<AuthUtil> authMockedStatic = mockStatic(AuthUtil.class)) {
            authMockedStatic.when(AuthUtil::getAuthenticatedUserEmail).thenReturn("rio@gmail.com");
            when(userService.getUserByEmail("rio@gmail.com")).thenReturn(Optional.empty());

            mockMvcTest.perform(get("/v1/user/self")).andExpect(status().isNotFound());
        }
    }

    @Test
    void testGetUser_Found()throws Exception{
        try(MockedStatic<AuthUtil> authMockedStatic = mockStatic(AuthUtil.class)) {
            authMockedStatic.when(AuthUtil::getAuthenticatedUserEmail).thenReturn("rio@gmail.com");
            when(userService.getUserByEmail("rio@gmail.com")).thenReturn(Optional.of(user));

            mockMvcTest.perform(get("/v1/user/self")).andExpect(status().isOk());
        }
    }

    @Test
    void testGetUser_Success() {
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            authUtil.when(AuthUtil::getAuthenticatedUserEmail).thenReturn(user.getEmail());
            when(userService.getUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

            ResponseEntity<User> response = userController.getUser();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(user, response.getBody());
        }
    }

    @Test
    void testgetUser_Unauthorized() throws Exception {
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            authUtil.when(AuthUtil::getAuthenticatedUserEmail).thenReturn(null);

            mockMvcTest.perform(get("/v1/user/self"))
                    .andExpect(status().isUnauthorized());
        }
    }
    @Test
    void testCreateUser() throws EmailNotAvailableException {
        User newUser = new User();
        newUser.setEmail("rio@gmail.com");
        newUser.setPassword("Pas5w0rd");
        newUser.setFirstName("Rio");
        newUser.setLastName("Janerio");

        when(userService.createUser(any(User.class))).thenReturn(user);

        ResponseEntity<User> response = userController.createUser(newUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody());
    }


    @Test
    void testcreateUser_EmailNotAvailable() throws Exception {
        User newUser = new User();
        newUser.setEmail("rio@gmail.com");
        newUser.setPassword("Pas5w0rd");
        newUser.setFirstName("Rio");
        newUser.setLastName("Janerio");

        when(userService.createUser(any(User.class))).thenThrow(new EmailNotAvailableException("Email already Exist"));

        ResponseEntity<User> response = userController.createUser(newUser);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already exist", response.getBody());
    }


    @Test
    void testupdateUser_Success() throws Exception {
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            User userUpdate = new User();
            userUpdate.setFirstName("UpdatedFirst");
            userUpdate.setLastName("UpdatedLast");
            userUpdate.setPassword("newpassword");

            authUtil.when(AuthUtil::getAuthenticatedUserEmail).thenReturn(user.getEmail());
            when(userService.getUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(userService.hashPassword("newpassword")).thenReturn("hashedNewPassword");

            ResponseEntity<User> response = userController.updateUser(userUpdate);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(userService).updateUser(argThat(user
                    -> user.getFirstName().equals("UpdatedFirst")
                    && user.getLastName().equals("UpdatedLast")
                    && user.getPassword().equals("hashedNewPassword")
                    && user.getAccountUpdated() != null
            ));
        }
    }

    @Test
    void testUpdateUser_Unauthorized() {
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            authUtil.when(AuthUtil::getAuthenticatedUserEmail).thenReturn(null);

            ResponseEntity<User> response = userController.updateUser(new User());

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }
    }

    @Test
    void testUpdateUser_NotFound() {
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            authUtil.when(AuthUtil::getAuthenticatedUserEmail).thenReturn(user.getEmail());
            when(userService.getUserByEmail(user.getEmail())).thenReturn(Optional.empty());

            ResponseEntity<User> response = userController.updateUser(new User());

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }


    @Test
    void testUpdateUser_BadRequest() {
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            User userUpdate = new User();
            userUpdate.setEmail("newemail@example.com");

            authUtil.when(AuthUtil::getAuthenticatedUserEmail).thenReturn(user.getEmail());
            when(userService.getUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

            ResponseEntity<User> response = userController.updateUser(userUpdate);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }
}

