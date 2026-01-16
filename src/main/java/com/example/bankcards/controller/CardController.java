package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // USER: список своих карт + фильтры
    // /api/cards/my?page=0&size=10&status=ACTIVE&last4=1234
    @GetMapping("/my")
    public Page<CardResponse> myCards(
            Pageable pageable,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String last4
    ) {
        return cardService.myCards(pageable, status, last4).map(CardMapper::toResponse);
    }

    // USER: получить одну свою карту
    @GetMapping("/{id}")
    public CardResponse myCard(@PathVariable Long id) {
        return CardMapper.toResponse(cardService.myCardById(id));
    }
}
