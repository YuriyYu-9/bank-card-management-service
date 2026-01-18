package com.example.bankcards.service;

import com.example.bankcards.dto.TransferCreateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.exception.ApiException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
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
public class TransferServiceImpl implements TransferService {

    private final CardRepository cardRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;

    public TransferServiceImpl(CardRepository cardRepository, TransferRepository transferRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Transfer createMyTransfer(TransferCreateRequest req) {
        Long ownerId = currentUserId();

        if (req.fromCardId().equals(req.toCardId())) {
            throw ApiException.badRequest("fromCardId and toCardId must be different");
        }

        Card from = cardRepository.findByIdAndOwnerId(req.fromCardId(), ownerId)
                .orElseThrow(() -> ApiException.notFound("From card not found"));
        Card to = cardRepository.findByIdAndOwnerId(req.toCardId(), ownerId)
                .orElseThrow(() -> ApiException.notFound("To card not found"));

        assertCardOperable(from, "From card");
        assertCardOperable(to, "To card");

        if (!from.getCurrency().equals(to.getCurrency())) {
            throw ApiException.badRequest("Cards must have the same currency");
        }

        long amount = req.amountCents();
        if (amount <= 0) {
            throw ApiException.badRequest("amountCents must be > 0");
        }

        if (from.getBalanceCents() < amount) {
            throw ApiException.conflict("Insufficient funds");
        }

        from.setBalanceCents(from.getBalanceCents() - amount);
        to.setBalanceCents(to.getBalanceCents() + amount);

        cardRepository.save(from);
        cardRepository.save(to);

        Transfer t = new Transfer();
        t.setFromCard(from);
        t.setToCard(to);
        t.setAmountCents(amount);

        return transferRepository.save(t);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transfer> myTransfers(Pageable pageable) {
        Long ownerId = currentUserId();
        return transferRepository.findByFromCardOwnerIdOrToCardOwnerId(ownerId, ownerId, pageable);
    }

    private void assertCardOperable(Card card, String label) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw ApiException.conflict(label + " is blocked");
        }

        boolean expired = CardExpiryUtils.isExpired(
                card.getExpiryMonth(),
                card.getExpiryYear(),
                YearMonth.now()
        );
        if (expired) {
            throw ApiException.conflict(label + " is expired");
        }
    }

    private Long currentUserId() {
        String username = SecurityUtils.currentUsername();
        if (username == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized");
        }
        return userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> ApiException.notFound("Current user not found"));
    }
}
