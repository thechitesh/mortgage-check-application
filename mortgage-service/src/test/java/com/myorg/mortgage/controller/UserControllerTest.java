package com.myorg.mortgage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.mortgage.auth.model.UserDto;
import com.myorg.mortgage.mapper.UserMapper;
import com.myorg.mortgage.model.User;
import com.myorg.mortgage.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.yaml")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserMapper userMapper;
    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void test_RegisterUser() throws Exception {
        UserDto userDto = UserDto.builder().username("user-1").password("password").build();
        User user = User.builder().username("user-1").password("password").build();
        when(userMapper.toUser(userDto)).thenReturn(user);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDto)))
                .andExpect(status().isAccepted());
    }

    @Test
    public void test_Login() throws Exception {
        UserDto userDto = UserDto.builder().username("user-1").password("password").build();
        String token = "mockToken";
        when(authenticationService.login(userDto.getUsername(), userDto.getPassword())).thenReturn(token);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(token));
    }

}
