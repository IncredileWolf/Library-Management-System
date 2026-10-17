package com.library.management.repository;

import com.library.management.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.save(User.builder().name("mohit").email("mohit@gmail.com").build());
        userRepository.save(User.builder().name("mohit").email("mohit2@gmail.com").build());
        userRepository.save(User.builder().name("asha").email("asha@gmail.com").build());
    }

    @Test
    void findByNameReturnsOnlyExactMatches() {
        List<User> users = userRepository.findByName("mohit");

        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(u -> "mohit".equals(u.getName())));
    }

    @Test
    void findByNameContainsReturnsPartialMatches() {
        assertEquals(2, userRepository.findByNameContains("ohi").size());
        assertEquals(1, userRepository.findByNameContains("sha").size());
    }

    @Test
    void findByEmailReturnsTheRightUser() {
        User user = userRepository.findByEmail("asha@gmail.com");

        assertNotNull(user);
        assertEquals("asha", user.getName());
    }

    @Test
    void findByEmailReturnsNullWhenMissing() {
        assertNull(userRepository.findByEmail("nobody@gmail.com"));
    }
}
