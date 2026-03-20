package com.innowise.userservice.integration;

import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.dto.UserResponseDto;
import com.innowise.userservice.dto.UserUpdateDto;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private UserRepository userRepository;

    private UserCreateDto pavelUser;
    private UserCreateDto romanUser;
    private UserUpdateDto updateUser;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api")
                .build();

        userRepository.deleteAll();

        pavelUser = new UserCreateDto();
        pavelUser.setName("Pavel");
        pavelUser.setSurname("Kiryanov");
        pavelUser.setEmail("PavelKiryanov.007@mail.com");
        pavelUser.setBirthDate(LocalDate.of(2002, 1, 8));

        romanUser = new UserCreateDto();
        romanUser.setName("Roman");
        romanUser.setSurname("Kirjanov");
        romanUser.setEmail("Roman@mail.com");
        romanUser.setBirthDate(LocalDate.of(2005, 5, 15));

        updateUser = new UserUpdateDto();
        updateUser.setName("PavelUpdated");
        updateUser.setSurname("KiryanovUpdated");
        updateUser.setEmail("PavelUpdated@mail.com");
        updateUser.setBirthDate(LocalDate.of(2002, 1, 8));
        updateUser.setActive(false);
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        UserResponseDto response = restClient.post()
                .uri("/users")
                .body(pavelUser)
                .retrieve()
                .onStatus(HttpStatus.CREATED::equals, (req, res) -> {})
                .body(UserResponseDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Pavel");
        assertThat(response.getSurname()).isEqualTo("Kiryanov");
        assertThat(response.getEmail()).isEqualTo("PavelKiryanov.007@mail.com");
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(2002, 1, 8));
        assertThat(response.isActive()).isTrue();

        assertThat(userRepository.findById(response.getId())).isPresent();
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturn400() {
        UserCreateDto invalidUser = new UserCreateDto();
        invalidUser.setName("Pavel");
        invalidUser.setSurname("Kiryanov");
        invalidUser.setEmail("not-an-email");
        invalidUser.setBirthDate(LocalDate.of(2002, 1, 8));

        ResponseEntity<String> response = restClient.post()
                .uri("/users")
                .body(invalidUser)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getUserById_ShouldReturnUser() {
        UserResponseDto createdUser = restClient.post()
                .uri("/users")
                .body(pavelUser)
                .retrieve()
                .onStatus(HttpStatus.CREATED::equals, (req, res) -> {})
                .body(UserResponseDto.class);

        Long userId = createdUser.getId();

        UserResponseDto response = restClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .body(UserResponseDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getName()).isEqualTo("Pavel");
        assertThat(response.getSurname()).isEqualTo("Kiryanov");
    }

    @Test
    void getUserById_NotFound_ShouldReturn404() {
        ResponseEntity<String> response = restClient.get()
                .uri("/users/999")
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllUsers_ShouldReturnPageWithBothUsers() {
        restClient.post().uri("/users").body(pavelUser).retrieve().toBodilessEntity();
        restClient.post().uri("/users").body(romanUser).retrieve().toBodilessEntity();

        Map<String, Object> response = restClient.get()
                .uri("/users?page=0&size=10")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response).isNotNull();
        assertThat(((Number) response.get("totalElements")).intValue()).isEqualTo(2);
        assertThat(((Number) response.get("totalPages")).intValue()).isEqualTo(1);
    }

    @Test
    void getAllUsers_WithFilterByNamePavel_ShouldReturnOnlyPavel() {
        restClient.post().uri("/users").body(pavelUser).retrieve().toBodilessEntity();
        restClient.post().uri("/users").body(romanUser).retrieve().toBodilessEntity();

        Map<String, Object> response = restClient.get()
                .uri("/users?name=Pavel")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response).isNotNull();

        int totalElements = ((Number) response.get("totalElements")).intValue();
        assertThat(totalElements).isEqualTo(1);

        var content = (java.util.List<Map<String, Object>>) response.get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("name")).isEqualTo("Pavel");
    }

    @Test
    void getAllUsers_WithFilterBySurnameKiryanov_ShouldReturnOnlyPavel() {
        restClient.post().uri("/users").body(pavelUser).retrieve().toBodilessEntity();
        restClient.post().uri("/users").body(romanUser).retrieve().toBodilessEntity();

        Map<String, Object> response = restClient.get()
                .uri("/users?surname=Kiryanov")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response).isNotNull();

        int totalElements = ((Number) response.get("totalElements")).intValue();
        assertThat(totalElements).isEqualTo(1);

        var content = (java.util.List<Map<String, Object>>) response.get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("surname")).isEqualTo("Kiryanov");
        assertThat(content.get(0).get("id")).isEqualTo(1);
    }

    @Test
    void getAllUsers_WithFilterByNameAndSurname_ShouldReturnPavel() {
        UserResponseDto createdPavel = restClient.post()
                .uri("/users")
                .body(pavelUser)
                .retrieve()
                .body(UserResponseDto.class);

        UserResponseDto createdRoman = restClient.post()
                .uri("/users")
                .body(romanUser)
                .retrieve()
                .body(UserResponseDto.class);

        Map<String, Object> response = restClient.get()
                .uri("/users?name=Pavel&surname=Kiryanov")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response).isNotNull();

        int totalElements = ((Number) response.get("totalElements")).intValue();
        assertThat(totalElements).isEqualTo(1);

        var content = (java.util.List<Map<String, Object>>) response.get("content");
        assertThat(content).hasSize(1);

        Map<String, Object> user = content.get(0);
        assertThat(user.get("name")).isEqualTo("Pavel");
        assertThat(user.get("surname")).isEqualTo("Kiryanov");
        assertThat(user.get("id")).isEqualTo(1);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        UserResponseDto createdUser = restClient.post()
                .uri("/users")
                .body(pavelUser)
                .retrieve()
                .body(UserResponseDto.class);

        Long userId = createdUser.getId();

        UserResponseDto response = restClient.put()
                .uri("/users/{id}", userId)
                .body(updateUser)
                .retrieve()
                .body(UserResponseDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getName()).isEqualTo("PavelUpdated");
        assertThat(response.getSurname()).isEqualTo("KiryanovUpdated");
        assertThat(response.getEmail()).isEqualTo("PavelUpdated@mail.com");
        assertThat(response.isActive()).isFalse();

        var updatedInDb = userRepository.findById(userId).get();
        assertThat(updatedInDb.getName()).isEqualTo("PavelUpdated");
        assertThat(updatedInDb.isActive()).isFalse();
    }

    @Test
    void updateUser_NotFound_ShouldReturn404() {
        ResponseEntity<String> response = restClient.put()
                .uri("/users/999")
                .body(updateUser)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        UserResponseDto createdUser = restClient.post()
                .uri("/users")
                .body(pavelUser)
                .retrieve()
                .body(UserResponseDto.class);

        Long userId = createdUser.getId();

        ResponseEntity<Void> response = restClient.delete()
                .uri("/users/{id}", userId)
                .exchange((request, responseSpec) ->
                        ResponseEntity.status(responseSpec.getStatusCode()).build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    void deleteUser_NotFound_ShouldReturn404() {
        ResponseEntity<String> response = restClient.delete()
                .uri("/users/999")
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void setUserActivity_ShouldChangeActiveStatus() {
        UserResponseDto createdUser = restClient.post()
                .uri("/users")
                .body(pavelUser)
                .retrieve()
                .body(UserResponseDto.class);

        Long userId = createdUser.getId();

        UserResponseDto deactivateResponse = restClient.patch()
                .uri("/users/{id}/active?active=false", userId)
                .retrieve()
                .body(UserResponseDto.class);

        assertThat(deactivateResponse).isNotNull();
        assertThat(deactivateResponse.isActive()).isFalse();

        UserResponseDto activateResponse = restClient.patch()
                .uri("/users/{id}/active?active=true", userId)
                .retrieve()
                .body(UserResponseDto.class);

        assertThat(activateResponse).isNotNull();
        assertThat(activateResponse.isActive()).isTrue();
    }
}