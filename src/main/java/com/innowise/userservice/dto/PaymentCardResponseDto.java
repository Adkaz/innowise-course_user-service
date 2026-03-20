package com.innowise.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PaymentCardResponseDto implements Serializable {
    private Long id;
    private Long userId;
    private String number;
    private String holder;
    private String expirationDate;
    private boolean active;
}
