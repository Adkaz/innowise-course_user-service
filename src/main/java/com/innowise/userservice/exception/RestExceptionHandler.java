package com.innowise.userservice.exception;

import com.innowise.userservice.exception.custom.CardsLimitExceededException;
import com.innowise.userservice.exception.custom.PaymentCardNotFoundException;
import com.innowise.userservice.exception.custom.UserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

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

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
                status.value(),
                "Validation Error " + validationErrors,
                request.getDescription(false)
        );

        return new ResponseEntity<>(exceptionResponse, headers, status);
    }
}
