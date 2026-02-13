package com.myorg.mortgage.controller;

import com.myorg.mortgage.auth.api.UserApi;
import com.myorg.mortgage.auth.model.LoginResponseDto;
import com.myorg.mortgage.auth.model.UserDto;
import com.myorg.mortgage.mapper.UserMapper;
import com.myorg.mortgage.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserMapper userMapper;
    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<Void> registerUser(UserDto userDto) {
        val user = userMapper.toUser(userDto);
        authenticationService.registerUser(user);
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<LoginResponseDto> login(UserDto userDto) {
        String token = authenticationService.login(userDto.getUsername(), userDto.getPassword());
        return ResponseEntity.ok(LoginResponseDto.builder().accessToken(token).build());
    }
}
