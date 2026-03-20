package com.innowise.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PaymentCardCreateDto implements Serializable {
    @NotBlank
    @Pattern(regexp = "^([0-9]{16})$")
    private String number;

    @NotBlank
    private String holder;

    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$")
    private String expirationDate;
}
