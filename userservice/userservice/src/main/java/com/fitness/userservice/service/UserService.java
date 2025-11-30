package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.model.Usermodel;
import com.fitness.userservice.repository.UserRepo;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepo repository;
    public UserResponse register( RegisterRequest request) {
        Usermodel user = new Usermodel();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        Usermodel saved_user = repository.save(user);

        UserResponse userResponse = new UserResponse();
        
         userResponse.setEmail(saved_user.getEmail());
         userResponse.setPassword(saved_user.getPassword());
         userResponse.setId(saved_user.getId());
        userResponse.setFirstName(saved_user.getFirstName());
         userResponse.setLastName(saved_user.getLastName());
         userResponse.setCreatedAt(saved_user.getCreatedAt());
         userResponse.setUpdatedAt(saved_user.getUpdatedAt());
         userResponse.setId((saved_user.getId()));
         return userResponse;
    }

    public UserResponse getUserProfile(String userid) {
        Usermodel user = repository.findById(userid).orElseThrow(()->new RuntimeException("user with id not found "));
        UserResponse userResponse = new UserResponse();

        userResponse.setEmail(user.getEmail());
        userResponse.setPassword(user.getPassword());
        userResponse.setId(user.getId());
        user.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        userResponse.setId((user.getId()));
        return userResponse;
    }

    public Boolean existByUserId(String userid) {

        log.info("calling user validation for user id{}", userid);
        return repository.existsById(userid);
    }
}
