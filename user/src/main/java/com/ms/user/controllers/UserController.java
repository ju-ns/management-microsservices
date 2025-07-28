package com.ms.user.controllers;

import com.ms.user.dtos.UserRecordDto;
import com.ms.user.models.UserModel;
import com.ms.user.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<UserModel> saveUser(@RequestBody @Valid UserRecordDto userRecordDto){
        var userModel = new UserModel();
        BeanUtils.copyProperties(userRecordDto, userModel);
        var savedUser = userService.save(userModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);

    }
    @GetMapping
    public ResponseEntity<List<UserModel>> getAllUsers(){
        List<UserModel> users = userService.findALL();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserModel> getUserById(@PathVariable UUID id){
        UserModel user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    @PutMapping("/{id}")
    ResponseEntity<UserModel> updateUser(@PathVariable UUID id, @RequestBody @Valid UserRecordDto userRecordDto){
        UserModel updateUser = userService.update(id, userRecordDto);
        return ResponseEntity.ok(updateUser);
    }
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
