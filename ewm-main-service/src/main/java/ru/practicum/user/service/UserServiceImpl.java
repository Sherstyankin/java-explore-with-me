package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.request.NewUserRequest;
import ru.practicum.entity.User;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by("id"));
        List<User> users;
        if (ids == null) {
            users = userRepository.findAll(page).getContent();
        } else {
            users = userRepository.findByIdIn(ids, page);
        }
        return UserMapper.mapToUserDto(users);
    }

    @Override
    public UserDto addUser(NewUserRequest dto) {
        User user = UserMapper.mapToUser(dto);
        return UserMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long userId) {
        getUserById(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь c ID:" + userId + " не найден."));
    }
}
