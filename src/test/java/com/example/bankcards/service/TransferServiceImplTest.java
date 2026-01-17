package com.example.bankcards.service;

import com.example.bankcards.dto.TransferCreateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ApiException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock CardRepository cardRepository;
    @Mock TransferRepository transferRepository;
    @Mock UserRepository userRepository;

    @InjectMocks TransferServiceImpl service;

    @BeforeEach
    void setup() {
        TestAuthHelper.clear();
    }

    @Test
    void createMyTransfer_ok_updatesBalancesAndSavesTransfer() {
        // arrange current user
        User u = new User();
        u.setId(2L);
        u.setUsername("user");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(u));

        Card from = card(10L, u, "USD", CardStatus.ACTIVE, 5000, notExpiredYear());
        Card to = card(11L, u, "USD", CardStatus.ACTIVE, 1000, notExpiredYear());

        TestAuthHelper.setAuth("user");

        when(cardRepository.findByIdAndOwnerId(10L, 2L)).thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndOwnerId(11L, 2L)).thenReturn(Optional.of(to));

        when(transferRepository.save(any(Transfer.class))).thenAnswer(inv -> {
            Transfer t = inv.getArgument(0, Transfer.class);
            t.setId(100L);
            return t;
        });

        // act
        Transfer result = service.createMyTransfer(new TransferCreateRequest(10L, 11L, 1500));

        // assert
        assertNotNull(result);
        assertEquals(100L, result.getId());

        assertEquals(3500, from.getBalanceCents());
        assertEquals(2500, to.getBalanceCents());

        verify(transferRepository, times(1)).save(any(Transfer.class));
    }

    @Test
    void createMyTransfer_insufficientFunds_conflict() {
        User u = new User();
        u.setId(2L);
        u.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(u));

        Card from = card(10L, u, "USD", CardStatus.ACTIVE, 100, notExpiredYear());
        Card to = card(11L, u, "USD", CardStatus.ACTIVE, 0, notExpiredYear());

        TestAuthHelper.setAuth("user");

        when(cardRepository.findByIdAndOwnerId(10L, 2L)).thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndOwnerId(11L, 2L)).thenReturn(Optional.of(to));

        ApiException ex = assertThrows(ApiException.class,
                () -> service.createMyTransfer(new TransferCreateRequest(10L, 11L, 1500)));

        assertEquals("CONFLICT", ex.getError());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void createMyTransfer_currencyMismatch_badRequest() {
        User u = new User();
        u.setId(2L);
        u.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(u));

        Card from = card(10L, u, "USD", CardStatus.ACTIVE, 5000, notExpiredYear());
        Card to = card(11L, u, "EUR", CardStatus.ACTIVE, 1000, notExpiredYear());

        TestAuthHelper.setAuth("user");

        when(cardRepository.findByIdAndOwnerId(10L, 2L)).thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndOwnerId(11L, 2L)).thenReturn(Optional.of(to));

        ApiException ex = assertThrows(ApiException.class,
                () -> service.createMyTransfer(new TransferCreateRequest(10L, 11L, 100)));

        assertEquals("BAD_REQUEST", ex.getError());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void createMyTransfer_blockedCard_conflict() {
        User u = new User();
        u.setId(2L);
        u.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(u));

        Card from = card(10L, u, "USD", CardStatus.BLOCKED, 5000, notExpiredYear());
        Card to = card(11L, u, "USD", CardStatus.ACTIVE, 0, notExpiredYear());

        TestAuthHelper.setAuth("user");

        when(cardRepository.findByIdAndOwnerId(10L, 2L)).thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndOwnerId(11L, 2L)).thenReturn(Optional.of(to));

        ApiException ex = assertThrows(ApiException.class,
                () -> service.createMyTransfer(new TransferCreateRequest(10L, 11L, 100)));

        assertEquals("CONFLICT", ex.getError());
        verify(transferRepository, never()).save(any());
    }

    private static Card card(Long id, User owner, String currency, CardStatus status, long balance, int expiryYear) {
        Card c = new Card();
        c.setId(id);
        c.setOwner(owner);
        c.setCurrency(currency);
        c.setStatus(status);
        c.setBalanceCents(balance);
        c.setExpiryMonth(12);
        c.setExpiryYear(expiryYear);
        return c;
    }

    private static int notExpiredYear() {
        return YearMonth.now().getYear() + 1;
    }
}
