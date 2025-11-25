package com.medivault.backend.service.auth.impl;

import com.medivault.backend.domain.common.Role;
import com.medivault.backend.domain.user.User;
import com.medivault.backend.dto.auth.RegisterRequest;
import com.medivault.backend.repository.UserRepository;
import com.medivault.backend.service.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request, Role role, UUID creatorId) {


        validateUniqueAccount(request.getEmail(), request.getRsaId());
        validateRoleSpecificFields(request,role,creatorId);

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .password(hashedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .rsaId(request.getRsaId())
                .dateOfBirth(request.getDateOfBirth()!=null? request.getDateOfBirth() : null)
                .phone(request.getPhone())
                .medicalAidNumber(request.getMedicalAid()!=null ? request.getMedicalAid() : null)
                .role(role)
                .isActive(!request.isProxy())
                .build();


        User saved = userRepository.save(user);
//        System.out.println(saved);
        if(request.isProxy()){
            sendTempCredentialsSms(saved, creatorId);
        }else{
            sendVerificationOTP(saved);
        }
        return saved;
    }

    private void validateUniqueAccount(String email, String rsaId) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (rsaId != null && userRepository.findByRsaId(rsaId).isPresent()) {
            throw new IllegalArgumentException("RSA ID already exists");
        }
    }

    private void validateRoleSpecificFields(RegisterRequest request, Role role, UUID creatorId) {
        if (role == Role.RECEPTION || role == Role.MANAGEMENT) {
            if (creatorId == null) {
                throw new IllegalArgumentException("Staff registration requires management approval");
            }
        }
    }

    private void sendTempCredentialsSms(User user, UUID creatorId){
        //temporary logins to user
    }

    private void sendVerificationOTP(User user){
        //OTP TO SMS OR EMAIL
    }

}
