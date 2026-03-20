package com.innowise.userservice.junit;

import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.dto.UserResponseDto;
import com.innowise.userservice.dto.UserUpdateDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.custom.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserCreateDto createDto;
    private UserResponseDto responseDto;
    private UserUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Pavel");
        user.setSurname("Kiryanov");
        user.setEmail("PavelKiryanov.007@mail.com");
        user.setBirthDate(LocalDate.of(2002, 1, 8));
        user.setActive(true);

        createDto = new UserCreateDto();
        createDto.setName("Pavel");
        createDto.setSurname("Kiryanov");
        createDto.setEmail("PavelKiryanov.007@mail.com");
        createDto.setBirthDate(LocalDate.of(2002, 1, 8));

        responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Pavel");
        responseDto.setSurname("Kiryanov");
        responseDto.setEmail("PavelKiryanov.007@mail.com");
        responseDto.setBirthDate(LocalDate.of(2002, 1, 8));
        responseDto.setActive(true);

        updateDto = new UserUpdateDto();
        updateDto.setName("Pavel");
        updateDto.setSurname("Kiryanov");
        updateDto.setEmail("PavelKiryanov.007@mail.com");
        updateDto.setBirthDate(LocalDate.of(2002, 1, 8));
        updateDto.setActive(true);
    }

    @Test
    void createUser_Success() {
        when(userMapper.toEntity(createDto)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Pavel");
        assertThat(result.getEmail()).isEqualTo("PavelKiryanov.007@mail.com");

        verify(userMapper).toEntity(createDto);
        verify(userRepository).save(user);
        verify(userMapper).toResponseDto(user);
    }

    @Test
    void getUserById_Success() {
        when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Pavel");

        verify(userRepository).getUserById(1L);
        verify(userMapper).toResponseDto(user);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.getUserById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository).getUserById(99L);
        verify(userMapper, never()).toResponseDto(any());
    }

    @Test
    void getAllUsersWithFilter_Success() {
        String name = "Pavel";
        String surname = "Kiryanov";
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        Page<UserResponseDto> result = userService.getAllUsersWithFilter(name, surname, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userMapper).toResponseDto(user);
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(userRepository).findById(1L);
        verify(userMapper).updateUserFromDto(updateDto, user);
        verify(userRepository).save(user);
        verify(userMapper).toResponseDto(user);
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, updateDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository).findById(99L);
        verify(userMapper, never()).updateUserFromDto(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository).existsById(99L);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void setUserActivity_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(user);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(responseDto);

        UserResponseDto result = userService.setUserActivity(1L, false);

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isActive()).isFalse();

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        assertThat(result).isNotNull();
    }

    @Test
    void setUserActivity_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setUserActivity(99L, true))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any());
    }
}
