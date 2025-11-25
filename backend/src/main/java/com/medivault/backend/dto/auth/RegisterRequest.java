package com.medivault.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RegisterRequest {

    @Email(message = "Invalid email")
    @NotBlank
    private String email;

    @NotBlank(message = "Password required")
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String rsaId;

    private LocalDate dateOfBirth;

    private String phone;

    private  String medicalAid;

    private boolean proxy=false;

}
