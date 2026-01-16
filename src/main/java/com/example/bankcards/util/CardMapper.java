package com.example.bankcards.util;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;

public final class CardMapper {

    private CardMapper() {
    }

    public static CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                maskPan(card.getPanLast4()),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getStatus(),
                card.getBalanceCents(),
                card.getCurrency(),
                card.getCreatedAt()
        );
    }

    private static String maskPan(String last4) {
        if (last4 == null || last4.length() != 4) {
            return "**** **** **** ????";
        }
        return "**** **** **** " + last4;
    }
}
