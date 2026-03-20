package com.innowise.userservice.exception.custom;

public class CardsLimitExceededException extends RuntimeException {
    public CardsLimitExceededException(String message) {
        super(message);
    }

    public CardsLimitExceededException(Long userId) {
        super("User with id " + userId + " already has 5 cards");
    }
}
