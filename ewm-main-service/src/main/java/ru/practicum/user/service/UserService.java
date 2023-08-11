package ru.practicum.user.service;

import ru.practicum.dto.UserDto;
import ru.practicum.dto.request.NewUserRequest;
import ru.practicum.entity.User;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto addUser(NewUserRequest dto);

    void deleteUser(Long userId);

    User getUserById(Long userId);
}
