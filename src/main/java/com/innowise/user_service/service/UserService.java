package com.innowise.user_service.service;

import com.innowise.user_service.dto.UserCreateDto;
import com.innowise.user_service.dto.UserResponseDto;
import com.innowise.user_service.dto.UserUpdateDto;
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
