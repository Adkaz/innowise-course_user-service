package com.innowise.user_service.repository;

import com.innowise.user_service.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

    Optional<PaymentCard> getPaymentCardById(Long id);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> getPaymentCardsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PaymentCard pc SET pc.active = :active WHERE pc.id = :id")
    int setPaymentCardActivity(@Param("id") Long id, @Param("active") boolean active);

    @Modifying
    @Query(value = "UPDATE payment_cards SET number = ?2, holder = ?3, expiration_date = ?4, active = ?5 WHERE id = ?1",
            nativeQuery = true)
    int updatePaymentCard(Long id, String number, String holder, String expirationDate, boolean active);
}
