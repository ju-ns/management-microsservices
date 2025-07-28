package com.ms.user.services;

import com.ms.user.dtos.UserRecordDto;
import com.ms.user.exceptions.ConflictException;
import com.ms.user.exceptions.InvalidUserDataException;
import com.ms.user.exceptions.UserNotFoundException;
import com.ms.user.models.UserModel;
import com.ms.user.producers.UserProducer;
import com.ms.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserProducer userProducer;

    @Transactional
    public UserModel save(UserModel userModel){
        boolean existsByEmail = userRepository.existsByEmail(userModel.getEmail());

        if(existsByEmail){
            throw new ConflictException("There's already a user with the email: " + userModel.getEmail());
        }
        userModel =  userRepository.save(userModel);
        if (userModel.getUserId() == null
                || userModel.getEmail() == null
                || userModel.getEmail().isBlank()
                || userModel.getName() == null
                || userModel.getName().isBlank()) {
            throw new InvalidUserDataException("User has null or empty required fields");
        }
        userProducer.publishMessageEmail(userModel);
        return userModel;
    }

    public List<UserModel> findALL(){
        return userRepository.findAll();
    }
    public UserModel findById(UUID id){
        return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
    @Transactional
    public UserModel update(UUID id, UserRecordDto userDto){
        Optional<UserModel> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty()){
            throw new UserNotFoundException("User not found with id: " + id);
        }
        UserModel userToUpdate = optionalUser.get();
        if(userDto.email() != null && !userDto.email().isBlank()) {
            String email = userDto.email().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            if (!email.equals(userToUpdate.getEmail()) && userRepository.existsByEmail(email)) {
                throw new ConflictException("There's already a user with the email" + email);
            }
            userToUpdate.setEmail(email);
        }
        if(userDto.name() != null && !userDto.name().isBlank()){
            String name = userDto.name().trim();
            if(name.length() < 3){
                throw new IllegalArgumentException("Name must be at least 3 characters long");
            }
            userToUpdate.setName(name);
        }
        BeanUtils.copyProperties(userDto, userToUpdate, "id");
        return userRepository.save(userToUpdate);
    }

    @Transactional
    public void delete(UUID id){
        userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
    }
}
