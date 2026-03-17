package com.innowise.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

public class PaymentCardCreateDto implements Serializable {
    @NotBlank
    @Pattern(regexp = "^([0-9]{16})$")
    private String number;

    @NotBlank
    private String holder;

    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$")
    private String expirationDate;

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getHolder() {
        return holder;
    }
    public void setHolder(String holder) {
        this.holder = holder;
    }

    public String getExpirationDate() {
        return expirationDate;
    }
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
