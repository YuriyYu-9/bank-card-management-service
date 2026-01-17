package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardState;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import org.springdoc.core.annotations.ParameterObject;
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

    @GetMapping("/my")
    public Page<CardResponse> myCards(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) CardState status,
            @RequestParam(required = false) String last4
    ) {
        return cardService.myCards(pageable, status, last4).map(CardMapper::toResponse);
    }

    @GetMapping("/{id}")
    public CardResponse myCard(@PathVariable Long id) {
        return CardMapper.toResponse(cardService.myCardById(id));
    }
}
