package com.codecool.gradebookapi.dto.mapper;

import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.model.ApplicationUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public ApplicationUser map(UserDto userDto) {
        return ApplicationUser.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .role(userDto.getRole())
                .build();
    }

    public UserDto map(ApplicationUser user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }

    public List<UserDto> mapAll(List<ApplicationUser> users) {
        return users.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
