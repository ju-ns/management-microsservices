package com.ms.user.services;

import com.ms.user.dtos.UserRecordDto;
import com.ms.user.exceptions.ConflictException;
import com.ms.user.exceptions.UserNotFoundException;
import com.ms.user.models.UserModel;
import com.ms.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Transactional
    public UserModel save(UserModel userModel){
        boolean existsByEmail = userRepository.existsByEmail(userModel.getEmail());

        if(existsByEmail){
            throw new ConflictException("There's already a user with the email: " + userModel.getEmail());
        }
        return userRepository.save(userModel);
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
        BeanUtils.copyProperties(userDto, userToUpdate, "id");
        return userRepository.save(userToUpdate);
    }

    @Transactional
    public void delete(UUID id){
        try{
            userRepository.deleteById(id);
        } catch(EmptyResultDataAccessException e){
            throw new UserNotFoundException("User not found with id: " + id);
        } catch(DataIntegrityViolationException e){
            throw new ConflictException("Cannot delete user with id: " + id + " due to related records.");
        }
    }
}
