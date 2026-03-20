package com.innowise.user_service.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExceptionResponseDto {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;

    public ExceptionResponseDto(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }
}
