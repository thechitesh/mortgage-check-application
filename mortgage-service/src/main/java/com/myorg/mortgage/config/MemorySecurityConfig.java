package com.myorg.mortgage.config;

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class MemorySecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        val admin = buildUserDetails("admin", encoder.encode("admin-pass"), "ADMIN");
        val tester = buildUserDetails("tester", encoder.encode("test-pass"), "USER");
        return new InMemoryUserDetailsManager(admin, tester);
    }

    private UserDetails buildUserDetails(String username, String encodedPassword, String role) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .roles(role)
                .build();
    }

}
