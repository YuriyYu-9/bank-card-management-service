package com.example.bankcards.util;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardState;
import com.example.bankcards.entity.Card;

import java.time.YearMonth;

public final class CardMapper {

    private CardMapper() {
    }

    public static CardResponse toResponse(Card card) {
        CardState state = resolveState(card);

        return new CardResponse(
                card.getId(),
                maskPan(card.getPanLast4()),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                state,
                card.getBalanceCents(),
                card.getCurrency(),
                card.getCreatedAt()
        );
    }

    private static CardState resolveState(Card card) {
        boolean expired = CardExpiryUtils.isExpired(
                card.getExpiryMonth(),
                card.getExpiryYear(),
                YearMonth.now()
        );

        if (expired) {
            return CardState.EXPIRED;
        }

        // Не истекла — отражаем хранимый статус
        return switch (card.getStatus()) {
            case ACTIVE -> CardState.ACTIVE;
            case BLOCKED -> CardState.BLOCKED;
        };
    }

    private static String maskPan(String last4) {
        if (last4 == null || last4.length() != 4) {
            return "**** **** **** ????";
        }
        return "**** **** **** " + last4;
    }
}
