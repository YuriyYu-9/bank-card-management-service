package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Card> findByOwnerIdAndStatus(Long ownerId, CardStatus status, Pageable pageable);

    Page<Card> findByOwnerIdAndPanLast4Containing(Long ownerId, String last4Part, Pageable pageable);

    Page<Card> findByOwnerIdAndStatusAndPanLast4Containing(Long ownerId, CardStatus status, String last4Part, Pageable pageable);

    Optional<Card> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    @Query("""
           select c
           from Card c
           where c.owner.id = :ownerId
             and (c.expiryYear < :year or (c.expiryYear = :year and c.expiryMonth < :month))
           """)
    Page<Card> findExpiredByOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("year") int year,
            @Param("month") int month,
            Pageable pageable
    );

    @Query("""
           select c
           from Card c
           where c.owner.id = :ownerId
             and c.panLast4 like concat('%', :last4Part, '%')
             and (c.expiryYear < :year or (c.expiryYear = :year and c.expiryMonth < :month))
           """)
    Page<Card> findExpiredByOwnerIdAndPanLast4Containing(
            @Param("ownerId") Long ownerId,
            @Param("last4Part") String last4Part,
            @Param("year") int year,
            @Param("month") int month,
            Pageable pageable
    );
}
