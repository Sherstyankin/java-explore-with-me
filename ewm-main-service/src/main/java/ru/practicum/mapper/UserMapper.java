package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.dto.request.NewUserRequest;
import ru.practicum.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class UserMapper {

    public User mapToUser(NewUserRequest dto) {
        return User.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .build();
    }

    public UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public UserShortDto mapToUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public List<UserDto> mapToUserDto(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }
}
