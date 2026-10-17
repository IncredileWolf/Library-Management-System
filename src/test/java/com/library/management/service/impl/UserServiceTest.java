package com.library.management.service.impl;

import com.library.management.dto.request.UserCreationRequest;
import com.library.management.dto.respone.UserCreationResponse;
import com.library.management.enums.Operator;
import com.library.management.enums.UserFilter;
import com.library.management.enums.UserStatus;
import com.library.management.enums.UserType;
import com.library.management.model.User;
import com.library.management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void addStudentForcesStudentTypeAndActiveStatus() {
        UserCreationRequest request = new UserCreationRequest();
        request.setUserName("Mohit");
        request.setUserEmail("mohit@gmail.com");
        request.setUserPhone("9999999999");
        request.setUserAddress("Mumbai");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserCreationResponse response = userService.addStudent(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(UserType.STUDENT, captor.getValue().getUserType());
        assertEquals(UserStatus.ACTIVE, captor.getValue().getUserStatus());
        assertEquals("mohit@gmail.com", response.getUserEmail());
        assertEquals("Mohit", response.getUserName());
    }

    @Test
    void filterByNameEqualsUsesExactMatch() {
        User u = User.builder().name("mohit").build();
        when(userRepository.findByName("mohit")).thenReturn(List.of(u));

        assertEquals(List.of(u), userService.filter(UserFilter.NAME, Operator.EQUALS, "mohit"));
    }

    @Test
    void filterByNameLikeUsesContains() {
        User u = User.builder().name("mohit").build();
        when(userRepository.findByNameContains("moh")).thenReturn(List.of(u));

        assertEquals(List.of(u), userService.filter(UserFilter.NAME, Operator.LIKE, "moh"));
    }

    @Test
    void filterByUnsupportedFieldCurrentlyReturnsEmptyList() {
        // Documents current behaviour: EMAIL / PHONE_NO are declared but not implemented.
        assertTrue(userService.filter(UserFilter.EMAIL, Operator.EQUALS, "x@y.com").isEmpty());
    }
}
