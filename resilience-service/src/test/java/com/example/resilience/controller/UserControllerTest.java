package com.example.resilience.controller;

import com.example.resilience.model.User;
import com.example.resilience.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getUser_returnsUser() throws Exception {
        User user = new User(1L, "testuser", "test@example.com", "Test", "User");
        when(userService.getUser(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void getUser_returnsFallback_whenServiceDegraded() throws Exception {
        User fallback = User.fallback(1L);
        when(userService.getUser(1L)).thenReturn(fallback);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEGRADED"))
                .andExpect(jsonPath("$.data.username").value("unknown"));
    }

    @Test
    void getAllUsers_returnsUserList() throws Exception {
        List<User> users = List.of(
                new User(1L, "alice", "alice@example.com", "Alice", "Smith"),
                new User(2L, "bob", "bob@example.com", "Bob", "Jones")
        );
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void evictUserCache_invokesService() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(userService, times(1)).evictUserCache(1L);
    }

    @Test
    void evictAllUserCache_invokesService() throws Exception {
        mockMvc.perform(delete("/api/v1/users/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(userService, times(1)).evictAllUserCache();
    }
}
