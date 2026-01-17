package com.example.bankcards.service;

import com.example.bankcards.dto.BlockRequestCreateRequest;
import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.BlockRequestStatus;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.ApiException;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardExpiryUtils;
import com.example.bankcards.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
public class BlockRequestServiceImpl implements BlockRequestService {

    private final BlockRequestRepository blockRequests;
    private final CardRepository cards;
    private final UserRepository users;

    public BlockRequestServiceImpl(BlockRequestRepository blockRequests, CardRepository cards, UserRepository users) {
        this.blockRequests = blockRequests;
        this.cards = cards;
        this.users = users;
    }

    @Override
    @Transactional
    public BlockRequest createMy(BlockRequestCreateRequest req) {
        Long ownerId = currentUserId();

        Card card = cards.findByIdAndOwnerId(req.cardId(), ownerId)
                .orElseThrow(() -> ApiException.notFound("Card not found"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw ApiException.conflict("Card is already blocked");
        }

        boolean expired = CardExpiryUtils.isExpired(card.getExpiryMonth(), card.getExpiryYear(), YearMonth.now());
        if (expired) {
            throw ApiException.conflict("Card is expired");
        }

        if (blockRequests.existsByCardIdAndStatus(card.getId(), BlockRequestStatus.PENDING)) {
            throw ApiException.conflict("Block request already pending for this card");
        }

        BlockRequest br = new BlockRequest();
        br.setCard(card);

        br.setRequestedBy(card.getOwner());

        br.setStatus(BlockRequestStatus.PENDING);
        return blockRequests.save(br);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlockRequest> myRequests(Pageable pageable) {
        Long ownerId = currentUserId();
        return blockRequests.findByRequestedById(ownerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlockRequest> adminList(Pageable pageable, BlockRequestStatus status, Long cardId) {
        if (status != null && cardId != null) {
            return blockRequests.findByStatusAndCardId(status, cardId, pageable);
        }
        if (status != null) {
            return blockRequests.findByStatus(status, pageable);
        }
        if (cardId != null) {
            return blockRequests.findByCardId(cardId, pageable);
        }
        return blockRequests.findAll(pageable);
    }

    @Override
    @Transactional
    public BlockRequest adminApprove(Long requestId) {
        BlockRequest br = blockRequests.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("Block request not found"));

        if (br.getStatus() != BlockRequestStatus.PENDING) {
            throw ApiException.conflict("Only PENDING request can be approved");
        }

        br.setStatus(BlockRequestStatus.APPROVED);

        Card card = br.getCard();
        if (card != null && card.getStatus() != CardStatus.BLOCKED) {
            card.setStatus(CardStatus.BLOCKED);
        }

        return blockRequests.save(br);
    }

    @Override
    @Transactional
    public BlockRequest adminReject(Long requestId) {
        BlockRequest br = blockRequests.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("Block request not found"));

        if (br.getStatus() != BlockRequestStatus.PENDING) {
            throw ApiException.conflict("Only PENDING request can be rejected");
        }

        br.setStatus(BlockRequestStatus.REJECTED);
        return blockRequests.save(br);
    }

    private Long currentUserId() {
        String username = SecurityUtils.currentUsername();
        if (username == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required");
        }
        return users.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> ApiException.notFound("Current user not found"));
    }
}
