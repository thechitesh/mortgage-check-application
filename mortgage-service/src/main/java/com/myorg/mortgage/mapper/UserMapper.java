package com.myorg.mortgage.mapper;

import com.myorg.mortgage.auth.model.UserDto;
import com.myorg.mortgage.model.Role;
import com.myorg.mortgage.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(UserDto dto) {
        return User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .role(Role.valueOf(dto.getRole()))
                .build();
    }

}
