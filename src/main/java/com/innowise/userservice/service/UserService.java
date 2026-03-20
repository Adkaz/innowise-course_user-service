package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.dto.UserResponseDto;
import com.innowise.userservice.dto.UserUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDto createUser(UserCreateDto userCreateDto);

    UserResponseDto getUserById(Long id);

    Page<UserResponseDto> getAllUsersWithFilter(String name, String surname, Pageable pageable);

    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);

    void deleteUser(Long id);

    UserResponseDto setUserActivity(Long id, boolean active);
}
