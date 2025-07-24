package com.ms.user.services;

import com.ms.user.dtos.UserRecordDto;
import com.ms.user.exceptions.ConflictException;
import com.ms.user.exceptions.UserNotFoundException;
import com.ms.user.models.UserModel;
import com.ms.user.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_WhenUserExists_ShouldReturnUser(){
        UUID id = UUID.randomUUID();
        UserModel user = new UserModel();
        user.setUserId(id);
        user.setEmail("test@gmail.com");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserModel found = userService.findById(id);

        assertEquals(id, found.getUserId());
        verify(userRepository).findById(id);
    }
    @Test
    void shouldThrowException_WhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findById(id));
    }
    @Test
    void testSaveUser_WhenEmailDoesNotExist_ShouldSaveUser(){
        UserModel user = new UserModel();
        user.setUserId(UUID.randomUUID());
        user.setEmail("unique_email@gmail.com");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        UserModel savedUser = userService.save(user);

        assertNotNull(savedUser);
        assertEquals(user.getEmail(), savedUser.getEmail());
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).save(user);
    }
    @Test
    void testSavedUser_WhenEmailAlreadyExists_ShouldThrowConflictException(){
        UserModel user = new UserModel();
        user.setUserId(UUID.randomUUID());
        user.setEmail("duplicate@gmail.com");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
        assertThrows(ConflictException.class, () -> userService.save(user));
        verify(userRepository, never()).save(any(UserModel.class));
    }
    @Test
    void testUpdateUser_WhenUserExists_ShouldUpdateUser(){
        UUID id = UUID.randomUUID();
        UserModel existingUser = new UserModel();
        existingUser.setUserId(id);
        existingUser.setEmail("old@gmail.com");

        UserRecordDto updatedUserDto = new UserRecordDto("New Name", "new@gmail.com");

        UserModel updatedUser = new UserModel();
        updatedUser.setEmail("new@gmail.com");
        updatedUser.setName("New Name");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(existingUser);

        UserModel result = userService.update(id, updatedUserDto);
        assertEquals("new@gmail.com", result.getEmail());
        verify(userRepository).save(any(UserModel.class));
    }
    @Test
    void testUpdateUser_WhenUserDoesNotExist_ShouldThrowException(){
        UUID id = UUID.randomUUID();
        UserRecordDto dto = new UserRecordDto("Name", "test@gmail.com");
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.update(id,dto));
    }
    @Test
    void testUpdateUser_WhenDataIsValid_ShouldUpdateUserSuccessfully(){
        UUID id = UUID.randomUUID();
        UserModel existing = new UserModel();
        UserRecordDto dto = new UserRecordDto("New Name", "new@gmail.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@gmail.com")).thenReturn(false);
        when(userRepository.save(any(UserModel.class))).thenReturn(existing);

        UserModel update = userService.update(id, dto);
        assertEquals("new@gmail.com", update.getEmail());
        assertEquals("New Name", update.getName());
        verify(userRepository).save(existing);
    }
    @Test
    void testUpdateUser_WhenEmailIsInvalid_ShouldThrowIllegalArgumentException(){
        UUID id = UUID.randomUUID();
        UserModel existingUser = new UserModel();
        UserRecordDto dto = new UserRecordDto("New Name", "invalid-email");
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()
                -> userService.update(id, dto));
        assertEquals("Invalid email format", exception.getMessage());
    }
    @Test
    void testUpdateUser_WhenEmailAlreadyExists_ShouldThrowConflictException(){
        UUID id = UUID.randomUUID();
        UserModel existing = new UserModel();
        UserRecordDto dto = new UserRecordDto("New Name", "existing@gmail.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("existing@gmail.com")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, ()
        -> userService.update(id,dto));
        assertEquals("There's already a user with the emailexisting@gmail.com", exception.getMessage());
    }
    @Test
    void testUpdateUser_WhenNameIsTooShort_ShouldThrowIllegalArgumentException(){
        UUID id = UUID.randomUUID();
        UserModel existing = new UserModel();
        UserRecordDto dto = new UserRecordDto("Al", "valid@gmail.com");
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()
        -> userService.update(id, dto));
        assertEquals("Name must be at least 3 characters long", exception.getMessage());
    }
    @Test
    void testDeleteUser_WhenUserExists_ShouldDeleteUser(){
        UUID id = UUID.randomUUID();
        UserModel user = new UserModel();
        user.setUserId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(id);

        userService.delete(id);
        verify(userRepository).deleteById(id);
    }
    @Test
    void testDeleteUser_WhenUserDoesNotExist_ShouldThrowException(){
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.delete(id));
    }
}
