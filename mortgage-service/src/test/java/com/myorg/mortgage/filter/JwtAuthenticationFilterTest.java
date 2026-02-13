package com.myorg.mortgage.filter;

import com.myorg.mortgage.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void testDoFilterInternal_withValidJwt() throws Exception {
        String jwt = "valid.jwt.token";
        String userEmail = "user@example.com";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUserName(jwt)).thenReturn(userEmail);
//        when(inMemoryUserDetailsManager.userDetailsService()).thenReturn(userDetailsService);
        when(inMemoryUserDetailsManager.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_withInvalidJwt() throws Exception {
        String jwt = "invalid.jwt.token";
        SecurityContextHolder.createEmptyContext();
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUserName(jwt)).thenThrow(new JwtException("Invalid JWT"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
//        assertNotNull(request.getAttribute("expires"));
    }

    @Test
    void testDoFilterInternal_withNoAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_withException() throws Exception {
        String jwt = "valid.jwt.token";
        String userEmail = "user@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUserName(jwt)).thenReturn(userEmail);
//        when(inMemoryUserDetailsManager.userDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(userEmail)).thenThrow(new RuntimeException("User service exception"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
//        assertNotNull(request.getAttribute("filter.exception"));
    }
}
