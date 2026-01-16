package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Card> findByOwnerIdAndStatus(Long ownerId, CardStatus status, Pageable pageable);

    // Поиск по последним 4 (простая фильтрация)
    Page<Card> findByOwnerIdAndPanLast4Containing(Long ownerId, String last4Part, Pageable pageable);
}
