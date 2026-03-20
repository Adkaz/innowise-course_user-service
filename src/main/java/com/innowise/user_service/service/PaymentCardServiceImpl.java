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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentCardServiceImpl implements PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    public PaymentCardServiceImpl(PaymentCardRepository paymentCardRepository, UserRepository userRepository, PaymentCardMapper paymentCardMapper) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
        this.paymentCardMapper = paymentCardMapper;
    }

    @Override
    @Transactional
    public PaymentCardResponseDto createPaymentCard(Long userId, PaymentCardCreateDto paymentCardCreateDto) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id:" + userId));

        List<PaymentCard> existingCards = paymentCardRepository.getPaymentCardsByUserId(userId);
        if (existingCards.size() >= 5) {
            throw new RuntimeException("User already has 5 cards");
        }

        PaymentCard newCard = paymentCardMapper.toEntity(paymentCardCreateDto);
        newCard.setUser(user);
        newCard.setActive(true);

        PaymentCard savedCard = paymentCardRepository.save(newCard);

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
