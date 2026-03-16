package com.innowise.user_service.service;

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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig("cards")
public class PaymentCardServiceImpl implements PaymentCardService {
    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    @Caching(
            put = {@CachePut(key = "#result.id")},
            evict = {@CacheEvict(key = "#userId")}
    )
    public PaymentCardResponseDto createPaymentCard(Long userId, PaymentCardCreateDto paymentCardCreateDto) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        int inserted = paymentCardRepository.createPaymentCard(
                userId,
                paymentCardCreateDto.getNumber(),
                paymentCardCreateDto.getHolder(),
                paymentCardCreateDto.getExpirationDate(),
                true
        );

        if (inserted == 0) {
            throw new CardsLimitExceededException(userId);
        }

        PaymentCard savedCard = paymentCardRepository
                .getPaymentCardsByUserId(userId)
                .stream()
                .filter(card -> card.getNumber().equals(paymentCardCreateDto.getNumber()))
                .findFirst()
                .orElseThrow(() -> new PaymentCardNotFoundException("Card not found after insert"));
        return paymentCardMapper.toResponseDto(savedCard);
    }

    @Override
    @Cacheable(key = "#id", sync = true)
    public PaymentCardResponseDto getPaymentCardById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.getPaymentCardById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        return paymentCardMapper.toResponseDto(paymentCard);
    }

    @Override
    @Cacheable(key = "#userId", sync = true)
    public List<PaymentCardResponseDto> getPaymentCardsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return paymentCardRepository.getPaymentCardsByUserId(userId)
                .stream()
                .map(paymentCardMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    @Caching(
            put = {@CachePut(key = "#result.id")},
            evict = {@CacheEvict(key = "#result.userId")}
    )
    public PaymentCardResponseDto updatePaymentCard(Long id, PaymentCardUpdateDto paymentCardUpdateDto) {
        PaymentCard paymentCard = paymentCardRepository.getPaymentCardById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        paymentCardMapper.updatePaymentCardFromDto(paymentCardUpdateDto, paymentCard);
        return paymentCardMapper.toResponseDto(paymentCardRepository.save(paymentCard));
    }

    @Override
    @Transactional
    @Caching(
            evict = {@CacheEvict(key = "#id"), @CacheEvict(key = "#result.userId")}
    )
    public PaymentCardResponseDto deletePaymentCard(Long id) {
        PaymentCard paymentCard = paymentCardRepository.getPaymentCardById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
        paymentCardRepository.deleteById(id);
        return paymentCardMapper.toResponseDto(paymentCard);
    }

    @Override
    @Transactional
    @Caching(
            put = {@CachePut(key = "#result.id")},
            evict = {@CacheEvict(key = "#result.userId")}
    )
    public PaymentCardResponseDto setPaymentCardActive(Long id, boolean active) {
        PaymentCard paymentCard = paymentCardRepository.getPaymentCardById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        paymentCard.setActive(active);
        return paymentCardMapper.toResponseDto(paymentCardRepository.save(paymentCard));
    }
}
