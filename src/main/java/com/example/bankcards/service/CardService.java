package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardState;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {

    Card create(CardCreateRequest req);

    Page<Card> myCards(Pageable pageable, CardState status, String last4);

    Card myCardById(Long cardId);

    Card adminChangeStatus(Long cardId, CardStatus status);

    void adminDelete(Long cardId);
}
