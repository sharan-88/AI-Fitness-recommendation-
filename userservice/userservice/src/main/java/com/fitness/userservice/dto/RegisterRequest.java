package com.fitness.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "email is requried")
    @Email(message ="invalid email ")
    private  String email;

    @NotBlank(message = "email is requried")
    @Size(min =6 , message ="password must be atleast 6 characters")
    private String password;
    private String firstName ;
    private String lastName;

}
