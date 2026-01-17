package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardState;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.ApiException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.PanCryptoService;
import com.example.bankcards.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final PanCryptoService panCryptoService;

    public CardServiceImpl(CardRepository cardRepository, UserRepository userRepository, PanCryptoService panCryptoService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.panCryptoService = panCryptoService;
    }

    @Override
    @Transactional
    public Card create(CardCreateRequest req) {
        var owner = userRepository.findById(req.ownerUserId())
                .orElseThrow(() -> ApiException.notFound("Owner user not found: id=" + req.ownerUserId()));

        String pan = req.pan().trim();
        if (pan.length() < 4) throw ApiException.badRequest("PAN is too short");
        String last4 = pan.substring(pan.length() - 4);

        var card = new Card();
        card.setOwner(owner);
        card.setPanLast4(last4);
        card.setPanEncrypted(panCryptoService.encrypt(pan));
        card.setExpiryMonth(req.expiryMonth());
        card.setExpiryYear(req.expiryYear());
        card.setCurrency(req.currency().trim().toUpperCase());
        card.setBalanceCents(req.initialBalanceCents());
        card.setStatus(CardStatus.ACTIVE);

        return cardRepository.save(card);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Card> myCards(Pageable pageable, CardState status, String last4) {
        Long ownerId = currentUserId();
        String last4Part = (last4 == null) ? null : last4.trim();
        boolean hasLast4 = last4Part != null && !last4Part.isBlank();

        // EXPIRED — вычисляемое: используем запрос по expiryYear/expiryMonth (с пагинацией корректно)
        if (status == CardState.EXPIRED) {
            YearMonth now = YearMonth.now();
            if (hasLast4) {
                return cardRepository.findExpiredByOwnerIdAndPanLast4Containing(ownerId, last4Part, now.getYear(), now.getMonthValue(), pageable);
            }
            return cardRepository.findExpiredByOwnerId(ownerId, now.getYear(), now.getMonthValue(), pageable);
        }

        // ACTIVE/BLOCKED
        if (status != null) {
            CardStatus dbStatus = switch (status) {
                case ACTIVE -> CardStatus.ACTIVE;
                case BLOCKED -> CardStatus.BLOCKED;
                case EXPIRED -> throw new IllegalStateException("EXPIRED handled above");
            };

            if (hasLast4) {
                return cardRepository.findByOwnerIdAndStatusAndPanLast4Containing(ownerId, dbStatus, last4Part, pageable);
            }
            return cardRepository.findByOwnerIdAndStatus(ownerId, dbStatus, pageable);
        }

        // без статуса
        if (hasLast4) {
            return cardRepository.findByOwnerIdAndPanLast4Containing(ownerId, last4Part, pageable);
        }
        return cardRepository.findByOwnerId(ownerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Card myCardById(Long cardId) {
        Long ownerId = currentUserId();
        return cardRepository.findByIdAndOwnerId(cardId, ownerId)
                .orElseThrow(() -> ApiException.notFound("Card not found"));
    }

    @Override
    @Transactional
    public Card adminChangeStatus(Long cardId, CardStatus status) {
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> ApiException.notFound("Card not found"));
        card.setStatus(status);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public void adminDelete(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw ApiException.notFound("Card not found");
        }
        cardRepository.deleteById(cardId);
    }

    private Long currentUserId() {
        String username = SecurityUtils.currentUsername();
        if (username == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required");
        }
        return userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> ApiException.notFound("Current user not found"));
    }
}
