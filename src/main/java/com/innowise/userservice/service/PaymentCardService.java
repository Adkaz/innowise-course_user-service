package com.innowise.userservice.service;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.dto.PaymentCardUpdateDto;

import java.util.List;

public interface PaymentCardService {
    PaymentCardResponseDto createPaymentCard(Long userId, PaymentCardCreateDto paymentCardCreateDto);

    PaymentCardResponseDto getPaymentCardById(Long id);

    List<PaymentCardResponseDto> getPaymentCardsByUserId(Long userId);

    PaymentCardResponseDto updatePaymentCard(Long id, PaymentCardUpdateDto paymentCardUpdateDto);

    PaymentCardResponseDto deletePaymentCard(Long id);

    PaymentCardResponseDto setPaymentCardActive(Long id, boolean active);
}
