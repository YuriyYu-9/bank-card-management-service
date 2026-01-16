package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusUpdateRequest;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    // ADMIN: создать карту
    @PostMapping
    public CardResponse create(@Valid @RequestBody CardCreateRequest req) {
        return CardMapper.toResponse(cardService.create(req));
    }

    // ADMIN: сменить статус
    @PatchMapping("/{id}/status")
    public CardResponse changeStatus(@PathVariable Long id, @Valid @RequestBody CardStatusUpdateRequest req) {
        return CardMapper.toResponse(cardService.adminChangeStatus(id, req.status()));
    }

    // ADMIN: удалить карту
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        cardService.adminDelete(id);
    }
}
