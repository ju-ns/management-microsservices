package com.ms.user.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.user.dtos.UserRecordDto;
import com.ms.user.exceptions.UserNotFoundException;
import com.ms.user.models.UserModel;
import com.ms.user.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockmvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UserModel userModel;

    @BeforeEach
    void setUp(){
        userId = UUID.randomUUID();
        userModel = new UserModel();
        userModel.setUserId(userId);
        userModel.setName("Name");
        userModel.setEmail("email@example.com");
    }
    @Test
    void testGetUserById_ShouldReturnUser() throws Exception {
        when(userService.findById(userId)).thenReturn(userModel);
        mockmvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.email").value("email@example.com"));
    }
    @Test
    void testGetUserById_WhenUserDoesNotExist_ShouldReturn404() throws Exception{
        when(userService.findById(userId)).thenThrow(new UserNotFoundException("User not found"));
        mockmvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
    @Test
    void testGetAllUsers_ShouldReturnListOfUsers() throws Exception{
        List<UserModel> users = List.of(userModel);
        when(userService.findALL()).thenReturn(users);

        mockmvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].name").value("Name"))
                .andExpect(jsonPath("$[0].email").value("email@example.com"));
    }
    @Test
    void testSaveUser_ShouldReturnCreateUser() throws Exception {
        UserRecordDto userRecordDto = new UserRecordDto("Name", "email@example.com");
        UserModel userModel = new UserModel();
        userModel.setUserId(userId);
        userModel.setName("Name");
        userModel.setEmail("email@example.com");

        when(userService.save(Mockito.any(UserModel.class))).thenReturn(userModel);

        mockmvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRecordDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.email").value("email@example.com"));
    }
    @Test
    void testCreateUser_InvalidEmail_ShouldReturnBadRequest() throws Exception{
        UserRecordDto invalidUser = new UserRecordDto("Name", "invalidEmail");
        mockmvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    void testCreateUser_BlanckName_ShouldReturnBadRequest() throws Exception {
        UserRecordDto invalidUser = new UserRecordDto("", "valid@example.com");
        mockmvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testCreateUser_BlankEmail_ShouldReturnBadRequest() throws Exception{
        UserRecordDto invalidDto = new UserRecordDto("Name", "");

        mockmvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testCreateUser_NullName_ShouldReturnBadResquest() throws Exception{
        var invalidUser = new UserRecordDto(null, "valid@example.com");
        mockmvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testCreateUser_NullEmail_ShouldReturnBadRequest() throws Exception {
        var invalidUser = new UserRecordDto("Valid Name", null);
        mockmvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testUpdateUser_ShouldUpdateUser() throws Exception{
        UserRecordDto dto = new UserRecordDto("Update Name", "update@example.com");
        UserModel updateModel = new UserModel();
        updateModel.setUserId(userId);
        updateModel.setName("Update Name");
        updateModel.setEmail("update@example.com");

        when(userService.update(Mockito.eq(userId), Mockito.any(UserRecordDto.class)))
                .thenReturn(updateModel);
        mockmvc.perform(put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Update Name"))
                .andExpect(jsonPath("$.email").value("update@example.com"));
    }
    @Test
    void testUpdateUser_UserNotFOund_ShouldReturnNotFound() throws Exception {
        UserRecordDto userRecordDto = new UserRecordDto("Any Name", "any@example.com");
        when(userService.update(Mockito.eq(userId), Mockito.any(UserRecordDto.class)))
                .thenThrow(new UserNotFoundException("User not found"));
        mockmvc.perform(put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRecordDto)))
                .andExpect(status().isNotFound());
    }
    @Test
    void testDeleteUser_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(userService).delete(userId);
        mockmvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());
    }
    @Test
    void testDeleteUser_UserNotFound_ShouldReturnNotFound() throws Exception{
        Mockito.doThrow(new UserNotFoundException("User not found"))
                .when(userService).delete(userId);
        mockmvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
    @Test
    void testUnexpectedException_ShouldReturn500() throws Exception{
        when(userService.findALL()).thenThrow(new RuntimeException("Internal error"));
        mockmvc.perform(get("/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Unexpected Error"))
                .andExpect(jsonPath("$.message").value("An internal error occured. Please contact support."));
    }

}
