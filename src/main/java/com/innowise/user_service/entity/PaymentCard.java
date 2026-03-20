package com.innowise.user_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_cards")
@Getter
@Setter
public class PaymentCard extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "number", nullable = false, length = 20)
    private String number;

    @Column(name = "holder", nullable = false, length = 100)
    private String holder;

    @Column(name = "expiration_date", nullable = false, length = 5)
    private String expirationDate;

    @Column(name = "active")
    private boolean active;
}
