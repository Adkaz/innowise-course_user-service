package com.innowise.user_service.controller;

import com.innowise.user_service.dto.PaymentCardCreateDto;
import com.innowise.user_service.dto.PaymentCardResponseDto;
import com.innowise.user_service.dto.PaymentCardUpdateDto;
import com.innowise.user_service.repository.PaymentCardRepository;
import com.innowise.user_service.service.PaymentCardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class PaymentCardController {
    private final PaymentCardService paymentCardService;

    public PaymentCardController(PaymentCardService paymentCardService) {
        this.paymentCardService = paymentCardService;
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<PaymentCardResponseDto> createPaymentCard(@PathVariable Long userId, @Valid @RequestBody PaymentCardCreateDto paymentCardCreateDto) {
        PaymentCardResponseDto responseDto = paymentCardService.createPaymentCard(userId, paymentCardCreateDto);
        return ResponseEntity.status(201).body(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> getPaymentCard(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardById(id));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PaymentCardResponseDto>> getPaymentCardByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardsByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> updatePaymentCard(@PathVariable Long id, @Valid @RequestBody PaymentCardUpdateDto paymentCardUpdateDto) {
        return ResponseEntity.ok(paymentCardService.updatePaymentCard(id, paymentCardUpdateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id) {
        paymentCardService.deletePaymentCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> setActivity(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(paymentCardService.setPaymentCardActive(id, active));
    }
}
