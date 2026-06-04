package com.example.meetings.service;

import com.example.meetings.model.User;
import com.example.meetings.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// jdbc:h2:file:C:/Users/winnie/Desktop/vvs/calendar/data/meetingsdb
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // create registration successfully
    @Test
    void newRegistration() {
        when(userRepository.existsByUsername("Oly")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = userService.register("Oly", "123@gmail.com", "123");

        assertEquals("Oly", result.getUsername());
        assertEquals("123@gmail.com", result.getEmail());
        assertEquals("encoded", result.getPasswordHash());
    }

    // throw exception if create an existed user
    @Test
    void userExisted() {
        when(userRepository.existsByUsername("Oly")).thenReturn(true);

        assertThrows(
            IllegalArgumentException.class, 
            () -> userService.register("Oly", "123@gmail.com", "123")
        );

        verify(userRepository).existsByUsername("Oly");
    }

    // get user
    @Test
    void getUser() {
        User user = new User("Oly", "123@gmail.com", "pwd");

        when(userRepository.findByUsername("Oly")).thenReturn(Optional.of(user));

        User result = userService.requireByUsername("Oly");

        assertEquals("Oly", result.getUsername());
        verify(userRepository).findByUsername("Oly");
    }

    // throw exception if get an inexisted user
    @Test
    void userInexisted() {
        when(userRepository.findByUsername("Oly")).thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class, 
            () -> userService.requireByUsername("Oly")
        );

        verify(userRepository).findByUsername("Oly");
    }
}