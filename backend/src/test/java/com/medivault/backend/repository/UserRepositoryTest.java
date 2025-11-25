package com.medivault.backend.repository;

import com.medivault.backend.config.TestSecurityConfig;
import com.medivault.backend.domain.common.Role;
import com.medivault.backend.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestSecurityConfig.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("passwordEncoder")
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldSaveUserWithHashedPassword(){
        String plainPassword = "securePass123!";
        String hashedPassword = passwordEncoder.encode(plainPassword);
        System.out.println(hashedPassword);
        User user = User.builder()
                .email("patient@gmail.com")
                .password(hashedPassword)
                .role(Role.PATIENT)
                .isActive(true)
                .build();

        User savedUser= userRepository.save(user);

        System.out.println(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("patient@gmail.com");
        assertThat(savedUser.getRole()).isEqualTo(Role.PATIENT);
        assertThat(savedUser.getPassword()).isEqualTo(hashedPassword);
        assertThat(passwordEncoder.matches(plainPassword, savedUser.getPassword())).isTrue();
        assertThat(savedUser.getPassword()).isNotEqualTo(plainPassword);

        User fetched = userRepository.findByEmail("patient@gmail.com").orElse(null);
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(savedUser.getId());
        assertThat(fetched.getPassword()).isEqualTo(hashedPassword);
    }

}
