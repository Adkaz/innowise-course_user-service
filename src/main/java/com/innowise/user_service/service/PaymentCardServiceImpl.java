package com.innowise.user_service.service;

import com.innowise.user_service.dto.PaymentCardCreateDto;
import com.innowise.user_service.dto.PaymentCardResponseDto;
import com.innowise.user_service.dto.PaymentCardUpdateDto;
import com.innowise.user_service.entity.PaymentCard;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.mapper.PaymentCardMapper;
import com.innowise.user_service.repository.PaymentCardRepository;
import com.innowise.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentCardServiceImpl implements PaymentCardService {
    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    public PaymentCardResponseDto createPaymentCard(Long userId, PaymentCardCreateDto paymentCardCreateDto) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id:" + userId));

        int inserted = paymentCardRepository.createPaymentCard(
                userId,
                paymentCardCreateDto.getNumber(),
                paymentCardCreateDto.getHolder(),
                paymentCardCreateDto.getExpirationDate(),
                true
        );

        if (inserted == 0) {
            throw new RuntimeException("User already has 5 cards");
        }

        PaymentCard savedCard = paymentCardRepository
                .getPaymentCardsByUserId(userId)
                .stream()
                .filter(card -> card.getNumber().equals(paymentCardCreateDto.getNumber()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Card not found after insert"));
        return paymentCardMapper.toResponseDto(savedCard);
    }

    @Override
    public PaymentCardResponseDto getPaymentCardById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.getPaymentCardById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));
        return paymentCardMapper.toResponseDto(paymentCard);
    }

    @Override
    public List<PaymentCardResponseDto> getPaymentCardsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return paymentCardRepository.getPaymentCardsByUserId(userId)
                .stream()
                .map(paymentCardMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public PaymentCardResponseDto updatePaymentCard(Long id, PaymentCardUpdateDto paymentCardUpdateDto) {
        PaymentCard paymentCard = paymentCardRepository.getPaymentCardById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));

        paymentCardMapper.updatePaymentCardFromDto(paymentCardUpdateDto, paymentCard);
        return paymentCardMapper.toResponseDto(paymentCardRepository.save(paymentCard));
    }

    @Override
    @Transactional
    public void deletePaymentCard(Long id) {
        if (!paymentCardRepository.existsById(id)) {
            throw new EntityNotFoundException("Card not found with id: " + id);
        }
        paymentCardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PaymentCardResponseDto setPaymentCardActive(Long id, boolean active) {
        PaymentCard paymentCard = paymentCardRepository.getPaymentCardById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));
        paymentCard.setActive(active);
        return paymentCardMapper.toResponseDto(paymentCardRepository.save(paymentCard));
    }
}
