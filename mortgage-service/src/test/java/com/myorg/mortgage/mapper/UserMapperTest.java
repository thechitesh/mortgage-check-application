package com.myorg.mortgage.mapper;

import com.myorg.mortgage.auth.model.UserDto;
import com.myorg.mortgage.model.Role;
import com.myorg.mortgage.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    UserMapper mapper = new UserMapper();

    @Test
    void test_UserMapping() {
        UserDto userDto = buildRequestDto();
        User user = mapper.toUser(userDto);
        assertThat(user).isEqualTo(buildUser());
    }

    private User buildUser() {
        return User.builder()
                .username("user-1")
                .password("pass-1")
                .role(Role.ROLE_USER)
                .build();
    }

    private UserDto buildRequestDto() {
        return UserDto.builder()
                .username("user-1")
                .password("pass-1")
                .role("ROLE_USER")
                .build();
    }

}
