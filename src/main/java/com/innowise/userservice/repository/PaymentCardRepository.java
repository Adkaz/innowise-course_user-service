package com.innowise.userservice.repository;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

    Optional<PaymentCard> getPaymentCardById(Long id);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> getPaymentCardsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "UPDATE payment_cards SET number = ?2, holder = ?3, expiration_date = ?4, active = ?5 WHERE id = ?1",
            nativeQuery = true)
    int updatePaymentCard(Long id, String number, String holder, String expirationDate, boolean active);
}
