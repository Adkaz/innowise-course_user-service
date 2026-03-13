package com.innowise.user_service.mapper;

import com.innowise.user_service.dto.UserCreateDto;
import com.innowise.user_service.dto.UserResponseDto;
import com.innowise.user_service.dto.UserUpdateDto;
import com.innowise.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserCreateDto userCreateDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User updateUserFromDto(UserUpdateDto userUpdateDto, @MappingTarget User user);
}
