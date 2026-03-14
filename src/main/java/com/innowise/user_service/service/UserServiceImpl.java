package com.innowise.user_service.service;

import com.innowise.user_service.dto.UserCreateDto;
import com.innowise.user_service.dto.UserResponseDto;
import com.innowise.user_service.dto.UserUpdateDto;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.exception.custom.UserNotFoundException;
import com.innowise.user_service.mapper.UserMapper;
import com.innowise.user_service.repository.UserRepository;
import com.innowise.user_service.specification.UserSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;


    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        User user = userMapper.toEntity(userCreateDto);
        user.setActive(true);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponseDto(user);
    }

    @Override
    public Page<UserResponseDto> getAllUsersWithFilter(String name, String surname, Pageable pageable) {
        Specification<User> spec = UserSpecification.byNameAndSurname(name, surname);
        Page<User> users = userRepository.getAllUsersWithFilter(spec, pageable);
        return users.map(userMapper::toResponseDto);
    }


    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new UserNotFoundException(id));
        userMapper.updateUserFromDto(userUpdateDto, user);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponseDto setUserActivity(Long id, boolean active) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(active);
        return userMapper.toResponseDto(userRepository.save(user));
    }
}
