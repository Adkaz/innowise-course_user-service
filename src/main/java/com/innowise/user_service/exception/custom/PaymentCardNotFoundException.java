package com.innowise.user_service.exception.custom;

public class PaymentCardNotFoundException extends RuntimeException {
    public PaymentCardNotFoundException(String message) {
        super(message);
    }

    public PaymentCardNotFoundException(Long id) {
        super("Card not found with id: " + id);
    }
}
