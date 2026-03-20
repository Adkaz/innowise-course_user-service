package com.innowise.user_service.integration;

import com.innowise.user_service.dto.PaymentCardCreateDto;
import com.innowise.user_service.dto.PaymentCardResponseDto;
import com.innowise.user_service.dto.PaymentCardUpdateDto;
import com.innowise.user_service.dto.UserCreateDto;
import com.innowise.user_service.dto.UserResponseDto;
import com.innowise.user_service.repository.PaymentCardRepository;
import com.innowise.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentCardControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    private UserCreateDto testUser;
    private PaymentCardCreateDto validCard;
    private PaymentCardCreateDto secondCard;
    private PaymentCardUpdateDto cardUpdateDto;
    private Long userId;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api")
                .build();

        testUser = new UserCreateDto();
        testUser.setName("Test");
        testUser.setSurname("User");
        testUser.setEmail("test.user@example.com");
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));

        UserResponseDto createdUser = restClient.post()
                .uri("/users")
                .body(testUser)
                .retrieve()
                .body(UserResponseDto.class);

        userId = createdUser.getId();

        validCard = new PaymentCardCreateDto();
        validCard.setNumber("4111111111111111");
        validCard.setExpirationDate("12/25");
        validCard.setHolder("Test User");

        secondCard = new PaymentCardCreateDto();
        secondCard.setNumber("5555555555554444");
        secondCard.setExpirationDate("12/26");
        secondCard.setHolder("Test User");

        cardUpdateDto = new PaymentCardUpdateDto();
        cardUpdateDto.setExpirationDate("12/27");
        cardUpdateDto.setHolder("Updated User");
        cardUpdateDto.setActive(true);
    }

    @Test
    void createPaymentCard_ShouldReturnCreatedCard() {
        PaymentCardResponseDto response = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .onStatus(HttpStatus.CREATED::equals, (req, res) -> {})
                .body(PaymentCardResponseDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getNumber()).isEqualTo("4111111111111111");
        assertThat(response.getExpirationDate()).isEqualTo("12/25");
        assertThat(response.getHolder()).isEqualTo("Test User");
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.isActive()).isTrue();

        assertThat(paymentCardRepository.findById(response.getId())).isPresent();
    }

    @Test
    void createPaymentCard_WithInvalidCardNumber_ShouldReturn400() {
        PaymentCardCreateDto invalidCard = new PaymentCardCreateDto();
        invalidCard.setNumber("123");
        invalidCard.setExpirationDate("12/25");
        invalidCard.setHolder("Test User");

        ResponseEntity<String> response = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(invalidCard)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createPaymentCard_WithInvalidExpirationDate_ShouldReturn400() {
        PaymentCardCreateDto invalidCard = new PaymentCardCreateDto();
        invalidCard.setNumber("4111111111111111");
        invalidCard.setExpirationDate("13/25");
        invalidCard.setHolder("Test User");

        ResponseEntity<String> response = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(invalidCard)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createPaymentCard_WithEmptyHolder_ShouldReturn400() {
        PaymentCardCreateDto invalidCard = new PaymentCardCreateDto();
        invalidCard.setNumber("4111111111111111");
        invalidCard.setExpirationDate("12/25");
        invalidCard.setHolder("");

        ResponseEntity<String> response = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(invalidCard)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createPaymentCard_ForNonExistentUser_ShouldReturn404() {
        ResponseEntity<String> response = restClient.post()
                .uri("/users/{userId}/cards", 999L)
                .body(validCard)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createPaymentCard_ExceedingLimit_ShouldReturn400() {
        for (int i = 0; i < 5; i++) {
            PaymentCardCreateDto card = new PaymentCardCreateDto();
            card.setNumber("411111111111111" + i);
            card.setExpirationDate("12/25");
            card.setHolder("Test User");

            restClient.post()
                    .uri("/users/{userId}/cards", userId)
                    .body(card)
                    .retrieve()
                    .toBodilessEntity();
        }

        ResponseEntity<String> response = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("already has 5 cards");
    }

    @Test
    void getPaymentCardById_ShouldReturnCard() {
        PaymentCardResponseDto createdCard = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        Long cardId = createdCard.getId();

        PaymentCardResponseDto response = restClient.get()
                .uri("/cards/{id}", cardId)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(cardId);
        assertThat(response.getNumber()).isEqualTo("4111111111111111");
        assertThat(response.getHolder()).isEqualTo("Test User");
        assertThat(response.getUserId()).isEqualTo(userId);
    }

    @Test
    void getPaymentCardById_NotFound_ShouldReturn404() {
        ResponseEntity<String> response = restClient.get()
                .uri("/cards/{id}", 999L)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPaymentCardsByUserId_ShouldReturnAllUserCards() {
        PaymentCardResponseDto firstCard = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        PaymentCardResponseDto secondCardResponse = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(secondCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        List<PaymentCardResponseDto> response = restClient.get()
                .uri("/users/{userId}/cards", userId)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<PaymentCardResponseDto>>() {});

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response).extracting("id").containsExactlyInAnyOrder(firstCard.getId(), secondCardResponse.getId());
        assertThat(response).extracting("userId").containsOnly(userId);
    }

    @Test
    void getPaymentCardsByUserId_WithNoCards_ShouldReturnEmptyList() {
        List<PaymentCardResponseDto> response = restClient.get()
                .uri("/users/{userId}/cards", userId)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<PaymentCardResponseDto>>() {});

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }

    @Test
    void getPaymentCardsByUserId_ForNonExistentUser_ShouldReturn404() {
        ResponseEntity<String> response = restClient.get()
                .uri("/users/{userId}/cards", 999L)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updatePaymentCard_ShouldReturnUpdatedCard() {
        PaymentCardResponseDto createdCard = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        Long cardId = createdCard.getId();

        PaymentCardResponseDto response = restClient.put()
                .uri("/cards/{id}", cardId)
                .body(cardUpdateDto)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(cardId);
        assertThat(response.getNumber()).isEqualTo("4111111111111111");
        assertThat(response.getExpirationDate()).isEqualTo("12/27");
        assertThat(response.getHolder()).isEqualTo("Updated User");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getUserId()).isEqualTo(userId);

        var updatedInDb = paymentCardRepository.findById(cardId).get();
        assertThat(updatedInDb.getExpirationDate()).isEqualTo("12/27");
        assertThat(updatedInDb.getHolder()).isEqualTo("Updated User");
        assertThat(updatedInDb.isActive()).isTrue();
    }

    @Test
    void updatePaymentCard_NotFound_ShouldReturn404() {
        ResponseEntity<String> response = restClient.put()
                .uri("/cards/{id}", 999L)
                .body(cardUpdateDto)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updatePaymentCard_WithInvalidExpirationDate_ShouldReturn400() {
        PaymentCardResponseDto createdCard = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        PaymentCardUpdateDto invalidUpdate = new PaymentCardUpdateDto();
        invalidUpdate.setExpirationDate("13/25");
        invalidUpdate.setHolder("Updated User");
        invalidUpdate.setActive(true);

        ResponseEntity<String> response = restClient.put()
                .uri("/cards/{id}", createdCard.getId())
                .body(invalidUpdate)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updatePaymentCard_WithInvalidHolder_ShouldReturn400() {
        PaymentCardResponseDto createdCard = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        PaymentCardUpdateDto invalidUpdate = new PaymentCardUpdateDto();
        invalidUpdate.setExpirationDate("12/27");
        invalidUpdate.setHolder("12345");
        invalidUpdate.setActive(true);

        ResponseEntity<String> response = restClient.put()
                .uri("/cards/{id}", createdCard.getId())
                .body(invalidUpdate)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deletePaymentCard_ShouldRemoveCard() {
        PaymentCardResponseDto createdCard = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        Long cardId = createdCard.getId();

        ResponseEntity<Void> response = restClient.delete()
                .uri("/cards/{id}", cardId)
                .exchange((request, responseSpec) ->
                        ResponseEntity.status(responseSpec.getStatusCode()).build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(paymentCardRepository.findById(cardId)).isEmpty();
    }

    @Test
    void deletePaymentCard_NotFound_ShouldReturn404() {
        ResponseEntity<String> response = restClient.delete()
                .uri("/cards/{id}", 999L)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void setPaymentCardActivity_ShouldChangeActiveStatus() {
        PaymentCardResponseDto createdCard = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        Long cardId = createdCard.getId();

        assertThat(createdCard.isActive()).isTrue();

        PaymentCardResponseDto deactivateResponse = restClient.patch()
                .uri("/cards/{id}?active=false", cardId)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        assertThat(deactivateResponse).isNotNull();
        assertThat(deactivateResponse.isActive()).isFalse();

        PaymentCardResponseDto activateResponse = restClient.patch()
                .uri("/cards/{id}?active=true", cardId)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        assertThat(activateResponse).isNotNull();
        assertThat(activateResponse.isActive()).isTrue();
    }

    @Test
    void setPaymentCardActivity_ForNonExistentCard_ShouldReturn404() {
        ResponseEntity<String> response = restClient.patch()
                .uri("/cards/{id}?active=false", 999L)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createMultipleCards_ShouldHaveUniqueIds() {
        PaymentCardResponseDto firstCard = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        PaymentCardResponseDto secondCardResponse = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(secondCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        assertThat(firstCard.getId()).isNotEqualTo(secondCardResponse.getId());
    }

    @Test
    void getPaymentCard_AfterDeletion_ShouldReturn404() {
        PaymentCardResponseDto createdCard = restClient.post()
                .uri("/users/{userId}/cards", userId)
                .body(validCard)
                .retrieve()
                .body(PaymentCardResponseDto.class);

        Long cardId = createdCard.getId();

        restClient.delete()
                .uri("/cards/{id}", cardId)
                .exchange((request, responseSpec) ->
                        ResponseEntity.status(responseSpec.getStatusCode()).build());

        ResponseEntity<String> response = restClient.get()
                .uri("/cards/{id}", cardId)
                .exchange((request, responseSpec) -> {
                    String body = new String(responseSpec.getBody().readAllBytes());
                    return ResponseEntity.status(responseSpec.getStatusCode()).body(body);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}