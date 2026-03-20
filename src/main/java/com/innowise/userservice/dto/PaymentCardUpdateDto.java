package com.innowise.userservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PaymentCardUpdateDto implements Serializable {

    @Pattern(regexp = "^[A-Za-z\\s'-]+$")
    private String holder;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$")
    private String expirationDate;

    @NotNull
    private Boolean active;
}
