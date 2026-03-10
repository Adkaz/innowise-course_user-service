package com.innowise.user_service.repository;

import com.innowise.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> getUserById(Long id);

    Page<User> getAllUsersWithFilter(Specification<User> spec, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.active = :active WHERE u.id = :id")
    int setUserActivity(@Param("id") Long id, @Param("active") boolean active);


    @Modifying
    @Query(value = "UPDATE users SET name =?2, surname = ?3, email = ?4, birth_date = ?5, active = ?6 WHERE id = ?1",
            nativeQuery = true)
    int updateUser(Long id, String name, String surname, String email, LocalDate birthDate, boolean active);

    @Modifying
    @Query(value = "INSERT INTO users (name, surname, email, birth_date, active)" +
            " VALUES(?1, ?2, ?3, ?4, ?5)", nativeQuery = true)
    int createUser(String name, String surname, String email, LocalDate birthDate, boolean active );
}
