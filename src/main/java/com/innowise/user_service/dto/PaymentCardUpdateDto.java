package com.innowise.user_service.dto;

public class PaymentCardUpdateDto {
    private String holder;
    private String expirationDate;
    private Boolean active;

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

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
}
