package com.rio_rishabhNEU.UserApp;

import com.rio_rishabhNEU.UserApp.Controllers.HealthzController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class HealthzControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @InjectMocks
    private HealthzController healthzController;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(healthzController).build();

        // Use lenient() to avoid UnnecessaryStubbingException in case the mock isn't used in some tests
        lenient().when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    public void testHealthCheckSuccess() throws Exception {
        // Mocking successful database connection
        when(dataSource.getConnection()).thenReturn(connection);

        mockMvc.perform(get("/healthz"))
                .andExpect(status().isOk());

        // Verify that getConnection() was called
        verify(dataSource).getConnection();
    }

    @Test
    public void testHealthCheckFailure() throws Exception {
        // Mocking failure in database connection
        when(dataSource.getConnection()).thenThrow(new SQLException());

        mockMvc.perform(get("/healthz"))
                .andExpect(status().isServiceUnavailable());

        // Verify that getConnection() was called
        verify(dataSource).getConnection();
    }

    @Test
    public void testBadRequestWithQueryParams() throws Exception {
        // Test case for bad request due to query params being passed
        mockMvc.perform(get("/healthz")
                .param("invalidParam", "value"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testBadRequestWithRequestBody() throws Exception {
        // Test case for bad request due to request body being passed
        mockMvc.perform(get("/healthz")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"key\": \"value\" }"))
                .andExpect(status().isBadRequest());
    }
}
