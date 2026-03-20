package com.innowise.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class UserUpdateDto implements Serializable {
    private String name;
    private String surname;
    private String email;
    private LocalDate birthDate;
    private Boolean active;
}
