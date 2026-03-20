package com.innowise.userservice.exception.custom;

public class PaymentCardNotFoundException extends RuntimeException {
    public PaymentCardNotFoundException(String message) {
        super(message);
    }

    public PaymentCardNotFoundException(Long id) {
        super("Card not found with id: " + id);
    }
}
