package com.example.resilience.service;

import com.example.resilience.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void getUser_returnsUser() {
        User user = userService.getUser(1L);

        assertNotNull(user);
        assertEquals(1L, user.id());
        assertNotNull(user.username());
    }

    @Test
    void getAllUsers_returnsUserList() {
        List<User> users = userService.getAllUsers();

        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    void fallbackUser_hasExpectedValues() {
        User fallback = User.fallback(99L);

        assertEquals(99L, fallback.id());
        assertEquals("unknown", fallback.username());
        assertEquals("unavailable@example.com", fallback.email());
    }

    @Test
    void evictUserCache_doesNotThrow() {
        assertDoesNotThrow(() -> userService.evictUserCache(1L));
    }

    @Test
    void evictAllUserCache_doesNotThrow() {
        assertDoesNotThrow(() -> userService.evictAllUserCache());
    }
}
