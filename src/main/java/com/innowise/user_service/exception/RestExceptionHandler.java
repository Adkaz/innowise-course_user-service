package com.innowise.user_service.exception;

import com.innowise.user_service.exception.custom.CardsLimitExceededException;
import com.innowise.user_service.exception.custom.PaymentCardNotFoundException;
import com.innowise.user_service.exception.custom.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handleUserNotFound(UserNotFoundException exception, WebRequest webRequest) {
        ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
                404,
                exception.getMessage(),
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentCardNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handlePaymentCardNotFound(PaymentCardNotFoundException exception, WebRequest webRequest) {
        ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
                404,
                exception.getMessage(),
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CardsLimitExceededException.class)
    public ResponseEntity<ExceptionResponseDto> handleCardsLimitExceeded(CardsLimitExceededException exception, WebRequest webRequest) {
        ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
                400,
                exception.getMessage(),
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}
