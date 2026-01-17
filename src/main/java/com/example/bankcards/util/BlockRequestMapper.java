package com.example.bankcards.util;

import com.example.bankcards.dto.BlockRequestResponse;
import com.example.bankcards.entity.BlockRequest;

public final class BlockRequestMapper {

    private BlockRequestMapper() {
    }

    public static BlockRequestResponse toResponse(BlockRequest br) {
        Long cardId = br.getCard() != null ? br.getCard().getId() : null;
        Long userId = br.getRequestedBy() != null ? br.getRequestedBy().getId() : null;

        return new BlockRequestResponse(
                br.getId(),
                cardId,
                userId,
                br.getStatus(),
                br.getCreatedAt()
        );
    }
}
