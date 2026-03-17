package com.innowise.user_service.junit;

import com.innowise.user_service.dto.PaymentCardCreateDto;
import com.innowise.user_service.dto.PaymentCardResponseDto;
import com.innowise.user_service.dto.PaymentCardUpdateDto;
import com.innowise.user_service.entity.PaymentCard;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.exception.custom.CardsLimitExceededException;
import com.innowise.user_service.exception.custom.PaymentCardNotFoundException;
import com.innowise.user_service.exception.custom.UserNotFoundException;
import com.innowise.user_service.mapper.PaymentCardMapper;
import com.innowise.user_service.repository.PaymentCardRepository;
import com.innowise.user_service.repository.UserRepository;
import com.innowise.user_service.service.PaymentCardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceUnitTests {

    @Mock
    private PaymentCardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper cardMapper;

    @InjectMocks
    private PaymentCardServiceImpl cardService;

    private User user;
    private PaymentCard card;
    private PaymentCardCreateDto createDto;
    private PaymentCardResponseDto responseDto;
    private PaymentCardUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Pavel");
        user.setActive(true);

        card = new PaymentCard();
        card.setId(1L);
        card.setNumber("1234567890123456");
        card.setHolder("PAVEL KIRYANOV");
        card.setExpirationDate("12/25");
        card.setActive(true);
        card.setUser(user);

        createDto = new PaymentCardCreateDto();
        createDto.setNumber("1234567890123456");
        createDto.setHolder("PAVEL KIRYANOV");
        createDto.setExpirationDate("12/25");

        responseDto = new PaymentCardResponseDto();
        responseDto.setId(1L);
        responseDto.setUserId(1L);
        responseDto.setNumber("1234567890123456");
        responseDto.setHolder("PAVEL KIRYANOV");
        responseDto.setExpirationDate("12/25");
        responseDto.setActive(true);

        updateDto = new PaymentCardUpdateDto();
        updateDto.setHolder("NEW HOLDER");
        updateDto.setExpirationDate("01/26");
        updateDto.setActive(false);
    }

    @Test
    void createPaymentCard_Success() {
        when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.createPaymentCard(
                eq(1L),
                eq("1234567890123456"),
                eq("PAVEL KIRYANOV"),
                eq("12/25"),
                eq(true)
        )).thenReturn(1);

        when(cardRepository.getPaymentCardsByUserId(1L)).thenReturn(List.of(card));
        when(cardMapper.toResponseDto(card)).thenReturn(responseDto);

        PaymentCardResponseDto result = cardService.createPaymentCard(1L, createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(userRepository).getUserById(1L);
        verify(cardRepository).createPaymentCard(
                eq(1L),
                eq("1234567890123456"),
                eq("PAVEL KIRYANOV"),
                eq("12/25"),
                eq(true)
        );
        verify(cardRepository).getPaymentCardsByUserId(1L);
        verify(cardMapper).toResponseDto(card);
    }

    @Test
    void createPaymentCard_UserNotFound_ThrowsException() {
        when(userRepository.getUserById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createPaymentCard(99L, createDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository).getUserById(99L);
        verify(cardRepository, never()).createPaymentCard(anyLong(), anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    void createPaymentCard_LimitExceeded_ThrowsException() {
        when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.createPaymentCard(
                eq(1L),
                eq("1234567890123456"),
                eq("PAVEL KIRYANOV"),
                eq("12/25"),
                eq(true)
        )).thenReturn(0);

        assertThatThrownBy(() -> cardService.createPaymentCard(1L, createDto))
                .isInstanceOf(CardsLimitExceededException.class)
                .hasMessageContaining("1");

        verify(userRepository).getUserById(1L);
        verify(cardRepository).createPaymentCard(
                eq(1L),
                eq("1234567890123456"),
                eq("PAVEL KIRYANOV"),
                eq("12/25"),
                eq(true)
        );
    }

    @Test
    void getPaymentCardById_Success() {
        when(cardRepository.getPaymentCardById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toResponseDto(card)).thenReturn(responseDto);

        PaymentCardResponseDto result = cardService.getPaymentCardById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(cardRepository).getPaymentCardById(1L);
        verify(cardMapper).toResponseDto(card);
    }

    @Test
    void getPaymentCardById_NotFound_ThrowsException() {
        when(cardRepository.getPaymentCardById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getPaymentCardById(99L))
                .isInstanceOf(PaymentCardNotFoundException.class)
                .hasMessageContaining("99");

        verify(cardRepository).getPaymentCardById(99L);
        verify(cardMapper, never()).toResponseDto(any());
    }

    @Test
    void getPaymentCardsByUserId_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.getPaymentCardsByUserId(1L)).thenReturn(List.of(card));
        when(cardMapper.toResponseDto(card)).thenReturn(responseDto);

        List<PaymentCardResponseDto> results = cardService.getPaymentCardsByUserId(1L);

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(1L);

        verify(userRepository).existsById(1L);
        verify(cardRepository).getPaymentCardsByUserId(1L);
        verify(cardMapper).toResponseDto(card);
    }

    @Test
    void getPaymentCardsByUserId_UserNotFound_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.getPaymentCardsByUserId(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository).existsById(99L);
        verify(cardRepository, never()).getPaymentCardsByUserId(any());
    }

    @Test
    void updatePaymentCard_Success() {
        when(cardRepository.getPaymentCardById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(PaymentCard.class))).thenReturn(card);
        when(cardMapper.toResponseDto(card)).thenReturn(responseDto);

        PaymentCardResponseDto result = cardService.updatePaymentCard(1L, updateDto);

        assertThat(result).isNotNull();

        verify(cardRepository).getPaymentCardById(1L);
        verify(cardMapper).updatePaymentCardFromDto(updateDto, card);
        verify(cardRepository).save(card);
        verify(cardMapper).toResponseDto(card);
    }

    @Test
    void updatePaymentCard_NotFound_ThrowsException() {
        when(cardRepository.getPaymentCardById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updatePaymentCard(99L, updateDto))
                .isInstanceOf(PaymentCardNotFoundException.class)
                .hasMessageContaining("99");

        verify(cardRepository).getPaymentCardById(99L);
        verify(cardMapper, never()).updatePaymentCardFromDto(any(), any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void deletePaymentCard_Success() {
        when(cardRepository.getPaymentCardById(1L)).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).deleteById(1L);
        when(cardMapper.toResponseDto(card)).thenReturn(responseDto);

        PaymentCardResponseDto result = cardService.deletePaymentCard(1L);

        assertThat(result).isNotNull();

        verify(cardRepository).getPaymentCardById(1L);
        verify(cardRepository).deleteById(1L);
        verify(cardMapper).toResponseDto(card);
    }

    @Test
    void deletePaymentCard_NotFound_ThrowsException() {
        when(cardRepository.getPaymentCardById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deletePaymentCard(99L))
                .isInstanceOf(PaymentCardNotFoundException.class)
                .hasMessageContaining("99");

        verify(cardRepository).getPaymentCardById(99L);
        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    void setPaymentCardActive_Success() {
        when(cardRepository.getPaymentCardById(1L)).thenReturn(Optional.of(card));

        ArgumentCaptor<PaymentCard> cardCaptor = ArgumentCaptor.forClass(PaymentCard.class);
        when(cardRepository.save(cardCaptor.capture())).thenReturn(card);
        when(cardMapper.toResponseDto(any(PaymentCard.class))).thenReturn(responseDto);

        PaymentCardResponseDto result = cardService.setPaymentCardActive(1L, false);

        PaymentCard savedCard = cardCaptor.getValue();
        assertThat(savedCard.isActive()).isFalse();

        verify(cardRepository).getPaymentCardById(1L);
        verify(cardRepository).save(any(PaymentCard.class));
        assertThat(result).isNotNull();
    }

    @Test
    void setPaymentCardActive_NotFound_ThrowsException() {
        when(cardRepository.getPaymentCardById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.setPaymentCardActive(99L, true))
                .isInstanceOf(PaymentCardNotFoundException.class)
                .hasMessageContaining("99");

        verify(cardRepository).getPaymentCardById(99L);
        verify(cardRepository, never()).save(any());
    }
}
