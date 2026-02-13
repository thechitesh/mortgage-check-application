package com.myorg.mortgage.service;

import com.myorg.mortgage.exception.MortgageAuthenticationException;
import com.myorg.mortgage.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;


    public void registerUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        inMemoryUserDetailsManager.createUser(user.toBuilder().password(encodedPassword).build());
    }

    public String login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        UserDetails userDetails = inMemoryUserDetailsManager.loadUserByUsername(username);
        return jwtService.generateToken(userDetails);
    }

    public Boolean validateToken(String token) {
        String username = jwtService.extractUserName(token);
        var user = inMemoryUserDetailsManager.loadUserByUsername(username);
        return jwtService.isTokenValid(token, user);
    }

    public boolean isUserAuthenticated() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String headerAuth = request.getHeader("Authorization");
                return headerAuth != null && validateToken(headerAuth.substring(7));
            }
            return false;
        } catch (Exception exp) {
            throw new MortgageAuthenticationException("User is not Authenticated");
        }

    }

}
