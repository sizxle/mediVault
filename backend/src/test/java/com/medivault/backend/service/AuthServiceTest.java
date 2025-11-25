package com.medivault.backend.service;

import com.medivault.backend.domain.common.Role;
import com.medivault.backend.domain.user.User;
import com.medivault.backend.dto.auth.RegisterRequest;
import com.medivault.backend.repository.UserRepository;
import com.medivault.backend.service.auth.AuthService;
import com.medivault.backend.service.auth.impl.AuthServiceImpl;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

//    @Test
//    void  shouldRegisterNewPatientWithSensitiveFieldsEncryptedAndReturnUser(){
//        String email= "patient@gmail.com";
//        String plainPassword="pass123!";
//        String hashedPassword="$2a$10$mockHash";
//        String rsaId= "9501011234567";
//        LocalDate dob= LocalDate.of(1995,1,1);
//        String medicalAid="AID123";
//
//        User savedUser= User.builder()
//                .id(UUID.randomUUID())
//                .email(email)
//                .password(hashedPassword)
//                .firstName("Sthembizo")
//                .lastName("Molefi")
//                .rsaId(rsaId)
//                .dateOfBirth(dob)
//                .phone("+27123466789")
//                .medicalAidNumber(medicalAid)
//                .role(Role.PATIENT)
//                .isActive(true)
//                .build();
//
//        when(passwordEncoder.encode(plainPassword)).thenReturn(hashedPassword);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
//        when(userRepository.findByRsaId(rsaId)).thenReturn(Optional.empty());
//        when(userRepository.save(any(User.class))).thenReturn(savedUser);
//
//
//        User registeredUser = authService.register(email,plainPassword,"Sthembizo", "Molefi",rsaId,dob,"+27123466789",medicalAid);
//
//
//        assertThat(registeredUser.getEmail()).isEqualTo(email);
//        assertThat(registeredUser.getPassword()).isEqualTo(hashedPassword);
//        assertThat(registeredUser.getRsaId()).isEqualTo(rsaId);
//        assertThat(registeredUser.getDateOfBirth()).isEqualTo(dob);
//        assertThat(registeredUser.getMedicalAidNumber()).isEqualTo(medicalAid);
//        assertThat(registeredUser.getRole()).isEqualTo(Role.PATIENT);
//
//    }

//
//    void shouldThrowOnDuplicateRsaId(){
//        String email= "new@gmail.com";
//        String rsaId= "9501011234567";
//
//        when(userRepository.findByRsaId(rsaId)).thenReturn(Optional.of(User.builder().build()));
//
//        assertThatThrownBy(() -> authService.register(email, "pass", "John", "Doe", rsaId, null, null, null))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("RSA ID already exists");
//
//    }


    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"PATIENT", "DOCTOR"})
    void shouldRegisterNewUserWithRole(Role role){

        String email= "sthembizo@gmail.com";
        String password="newPass123!";
        String hashed = "$2a$10$mockHash";

        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .password(password)
                .firstName("Test")
                .lastName("User")
                .rsaId("9501011234567")
                .phone("+27123456789")
                .build();

        if (role == Role.PATIENT) {
            request.setMedicalAid("AID123");
        }

        User savedMockUser= User.builder().id(UUID.randomUUID()).role(role).build();

        when(passwordEncoder.encode(request.getPassword())).thenReturn(hashed);
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByRsaId(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered= authService.register(request,role,null);

        assertThat(registered.getRole()).isEqualTo(role);
        assertThat(registered.getPassword()).isEqualTo(hashed);
        assertThat(registered.getEmail()).isEqualTo(email);

        verify(userRepository).save(any(User.class));

    }

    @Test
    void shouldThrowForManagementWithoutCreator() {
        RegisterRequest request = RegisterRequest.builder().email("admin@test.com").build();
        assertThatThrownBy(() -> authService.register(request, Role.MANAGEMENT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("management approval");
    }

    @Test
    void shouldSetPendingForProxyOnBehalf() {

        RegisterRequest request = RegisterRequest.builder()
                .email("proxy@test.com")
                .password("pass")
                .firstName("Proxy")
                .lastName("User")
                .rsaId("1234567890123")
                .proxy(true)
                .build();

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User registered = authService.register(request, Role.PATIENT, UUID.randomUUID());


        assertThat(registered.isActive()).isFalse();  // Pending
    }


    @Test
    void shouldThrowExceptionOnDuplicateEmail(){

        String email="existing@gmail.com";
        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder().build()));

        assertThatThrownBy(()->authService.register(request,Role.PATIENT,null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
    }
}
