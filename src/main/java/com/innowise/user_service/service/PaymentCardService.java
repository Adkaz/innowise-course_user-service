package com.innowise.user_service.service;

import com.innowise.user_service.dto.PaymentCardCreateDto;
import com.innowise.user_service.dto.PaymentCardResponseDto;
import com.innowise.user_service.dto.PaymentCardUpdateDto;

import java.util.List;

public interface PaymentCardService {
    PaymentCardResponseDto createPaymentCard(Long userId, PaymentCardCreateDto paymentCardCreateDto);

    PaymentCardResponseDto getPaymentCardById(Long id);

    List<PaymentCardResponseDto> getPaymentCardsByUserId(Long userId);

    PaymentCardResponseDto updatePaymentCard(Long id, PaymentCardUpdateDto paymentCardUpdateDto);

    void deletePaymentCard(Long id);

    PaymentCardResponseDto setPaymentCardActive(Long id, boolean active);
}
