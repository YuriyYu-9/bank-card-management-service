package com.example.bankcards.util;

import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Transfer;

public final class TransferMapper {

    private TransferMapper() {
    }

    public static TransferResponse toResponse(Transfer t) {
        String currency = (t.getFromCard() != null) ? t.getFromCard().getCurrency() : null;

        return new TransferResponse(
                t.getId(),
                t.getFromCard() != null ? t.getFromCard().getId() : null,
                t.getToCard() != null ? t.getToCard().getId() : null,
                t.getAmountCents(),
                currency,
                t.getCreatedAt()
        );
    }
}
