package com.fitness.userservice.repository;

import com.fitness.userservice.model.Usermodel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<Usermodel, String> {
    boolean existsByEmail(String email);
    Usermodel findByEmail(String email);
}