package com.innowise.user_service.service;

import com.innowise.user_service.dto.UserCreateDto;
import com.innowise.user_service.dto.UserResponseDto;
import com.innowise.user_service.dto.UserUpdateDto;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.mapper.UserMapper;
import com.innowise.user_service.repository.UserRepository;
import com.innowise.user_service.specification.UserSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

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
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toResponseDto(user);
    }

    @Override
    public Page<UserResponseDto> getAllUsersWithFilter(String name, String surname, Pageable pageable) {
        Specification<User> spec = UserSpecification.byNameAndSurname(name, surname);
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(userMapper::toResponseDto);
    }


    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        userMapper.updateUserFromDto(userUpdateDto, user);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponseDto setUserActivity(Long id, boolean active) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setActive(active);
        return userMapper.toResponseDto(userRepository.save(user));
    }
}
