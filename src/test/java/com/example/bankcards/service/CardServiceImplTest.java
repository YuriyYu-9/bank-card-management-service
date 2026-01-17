package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardState;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ApiException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.PanCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock CardRepository cardRepository;
    @Mock UserRepository userRepository;
    @Mock PanCryptoService panCryptoService;

    @InjectMocks CardServiceImpl service;

    @BeforeEach
    void setup() {
        TestAuthHelper.clear();
    }

    @Test
    void create_ok_setsLast4_encrypts_pan_normalizesCurrency_andSaves() {
        // arrange
        User owner = new User();
        owner.setId(2L);
        owner.setUsername("user");

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(panCryptoService.encrypt("4242424242424242")).thenReturn("enc-pan");

        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> {
            Card c = inv.getArgument(0, Card.class);
            c.setId(10L);
            return c;
        });

        CardCreateRequest req = new CardCreateRequest(
                "4242424242424242",
                12,
                2030,
                " usd ",
                50_000L,
                2L
        );

        // act
        Card saved = service.create(req);

        // assert
        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        assertNotNull(saved.getOwner());
        assertEquals(2L, saved.getOwner().getId());

        assertEquals("4242", saved.getPanLast4());
        assertEquals("enc-pan", saved.getPanEncrypted());
        assertEquals("USD", saved.getCurrency());

        assertEquals(50_000L, saved.getBalanceCents());
        assertEquals(CardStatus.ACTIVE, saved.getStatus());

        verify(panCryptoService, times(1)).encrypt("4242424242424242");
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void create_ownerNotFound_throws404() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () ->
                service.create(new CardCreateRequest(
                        "4242424242424242",
                        12,
                        2030,
                        "USD",
                        0L,
                        2L
                ))
        );

        assertEquals("NOT_FOUND", ex.getError());
        assertTrue(ex.getMessage().contains("Owner user not found"));
        verify(cardRepository, never()).save(any());
        verify(panCryptoService, never()).encrypt(anyString());
    }

    @Test
    void create_panTooShort_badRequest() {
        User owner = new User();
        owner.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        ApiException ex = assertThrows(ApiException.class, () ->
                service.create(new CardCreateRequest(
                        "123",
                        12,
                        2030,
                        "USD",
                        0L,
                        2L
                ))
        );

        assertEquals("BAD_REQUEST", ex.getError());
        assertTrue(ex.getMessage().toLowerCase().contains("pan is too short"));
        verify(cardRepository, never()).save(any());
        verify(panCryptoService, never()).encrypt(anyString());
    }

    @Test
    void myCards_noFilters_callsFindByOwnerId() {
        // arrange
        mockCurrentUser("user", 2L);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<Card> expected = new PageImpl<>(List.of(), pageable, 0);

        when(cardRepository.findByOwnerId(eq(2L), eq(pageable))).thenReturn(expected);

        // act
        Page<Card> page = service.myCards(pageable, null, null);

        // assert
        assertSame(expected, page);
        verify(cardRepository, times(1)).findByOwnerId(2L, pageable);

        verify(cardRepository, never()).findByOwnerIdAndPanLast4Containing(anyLong(), anyString(), any());
        verify(cardRepository, never()).findByOwnerIdAndStatus(anyLong(), any(), any());
        verify(cardRepository, never()).findExpiredByOwnerId(anyLong(), anyInt(), anyInt(), any());
    }

    @Test
    void myCards_last4Only_callsFindByOwnerIdAndPanLast4Containing() {
        // arrange
        mockCurrentUser("user", 2L);

        Pageable pageable = PageRequest.of(1, 5);
        Page<Card> expected = new PageImpl<>(List.of(), pageable, 0);

        when(cardRepository.findByOwnerIdAndPanLast4Containing(eq(2L), eq("42"), eq(pageable)))
                .thenReturn(expected);

        // act
        Page<Card> page = service.myCards(pageable, null, " 42 ");

        // assert
        assertSame(expected, page);
        verify(cardRepository, times(1)).findByOwnerIdAndPanLast4Containing(2L, "42", pageable);
        verify(cardRepository, never()).findByOwnerId(anyLong(), any());
    }

    @Test
    void myCards_activeWithoutLast4_callsFindByOwnerIdAndStatus() {
        // arrange
        mockCurrentUser("user", 2L);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Card> expected = new PageImpl<>(List.of(), pageable, 0);

        when(cardRepository.findByOwnerIdAndStatus(eq(2L), eq(CardStatus.ACTIVE), eq(pageable)))
                .thenReturn(expected);

        // act
        Page<Card> page = service.myCards(pageable, CardState.ACTIVE, null);

        // assert
        assertSame(expected, page);
        verify(cardRepository, times(1)).findByOwnerIdAndStatus(2L, CardStatus.ACTIVE, pageable);
        verify(cardRepository, never()).findByOwnerId(anyLong(), any());
    }

    @Test
    void myCards_blockedWithLast4_callsFindByOwnerIdAndStatusAndPanLast4Containing() {
        // arrange
        mockCurrentUser("user", 2L);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Card> expected = new PageImpl<>(List.of(), pageable, 0);

        when(cardRepository.findByOwnerIdAndStatusAndPanLast4Containing(eq(2L), eq(CardStatus.BLOCKED), eq("7777"), eq(pageable)))
                .thenReturn(expected);

        // act
        Page<Card> page = service.myCards(pageable, CardState.BLOCKED, "7777");

        // assert
        assertSame(expected, page);
        verify(cardRepository, times(1))
                .findByOwnerIdAndStatusAndPanLast4Containing(2L, CardStatus.BLOCKED, "7777", pageable);
        verify(cardRepository, never()).findByOwnerIdAndStatus(anyLong(), any(), any());
    }

    @Test
    void myCards_expiredWithoutLast4_callsFindExpiredByOwnerId() {
        // arrange
        mockCurrentUser("user", 2L);

        YearMonth now = YearMonth.now();
        Pageable pageable = PageRequest.of(0, 20);

        Page<Card> expected = new PageImpl<>(List.of(), pageable, 0);

        when(cardRepository.findExpiredByOwnerId(eq(2L), eq(now.getYear()), eq(now.getMonthValue()), eq(pageable)))
                .thenReturn(expected);

        // act
        Page<Card> page = service.myCards(pageable, CardState.EXPIRED, null);

        // assert
        assertSame(expected, page);
        verify(cardRepository, times(1))
                .findExpiredByOwnerId(2L, now.getYear(), now.getMonthValue(), pageable);

        verify(cardRepository, never()).findByOwnerId(anyLong(), any());
        verify(cardRepository, never()).findByOwnerIdAndStatus(anyLong(), any(), any());
    }

    @Test
    void myCards_expiredWithLast4_callsFindExpiredByOwnerIdAndPanLast4Containing() {
        // arrange
        mockCurrentUser("user", 2L);

        YearMonth now = YearMonth.now();
        Pageable pageable = PageRequest.of(0, 20);

        Page<Card> expected = new PageImpl<>(List.of(), pageable, 0);

        when(cardRepository.findExpiredByOwnerIdAndPanLast4Containing(eq(2L), eq("1234"), eq(now.getYear()), eq(now.getMonthValue()), eq(pageable)))
                .thenReturn(expected);

        // act
        Page<Card> page = service.myCards(pageable, CardState.EXPIRED, " 1234 ");

        // assert
        assertSame(expected, page);
        verify(cardRepository, times(1))
                .findExpiredByOwnerIdAndPanLast4Containing(2L, "1234", now.getYear(), now.getMonthValue(), pageable);

        verify(cardRepository, never()).findExpiredByOwnerId(anyLong(), anyInt(), anyInt(), any());
    }

    @Test
    void myCardById_ok_returnsCardForOwner() {
        // arrange
        mockCurrentUser("user", 2L);

        Card card = new Card();
        card.setId(10L);

        when(cardRepository.findByIdAndOwnerId(10L, 2L)).thenReturn(Optional.of(card));

        // act
        Card result = service.myCardById(10L);

        // assert
        assertSame(card, result);
        verify(cardRepository, times(1)).findByIdAndOwnerId(10L, 2L);
    }

    @Test
    void myCardById_notFound_throws404() {
        // arrange
        mockCurrentUser("user", 2L);
        when(cardRepository.findByIdAndOwnerId(10L, 2L)).thenReturn(Optional.empty());

        // act
        ApiException ex = assertThrows(ApiException.class, () -> service.myCardById(10L));

        // assert
        assertEquals("NOT_FOUND", ex.getError());
    }

    @Test
    void adminChangeStatus_ok_updatesStatus_andSaves() {
        Card card = new Card();
        card.setId(10L);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0, Card.class));

        Card updated = service.adminChangeStatus(10L, CardStatus.BLOCKED);

        assertNotNull(updated);
        assertEquals(CardStatus.BLOCKED, updated.getStatus());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void adminChangeStatus_notFound_throws404() {
        when(cardRepository.findById(10L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.adminChangeStatus(10L, CardStatus.BLOCKED));
        assertEquals("NOT_FOUND", ex.getError());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void adminDelete_ok_deletesWhenExists() {
        when(cardRepository.existsById(10L)).thenReturn(true);

        service.adminDelete(10L);

        verify(cardRepository, times(1)).deleteById(10L);
    }

    @Test
    void adminDelete_notFound_throws404_andDoesNotDelete() {
        when(cardRepository.existsById(10L)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> service.adminDelete(10L));
        assertEquals("NOT_FOUND", ex.getError());
        verify(cardRepository, never()).deleteById(anyLong());
    }

    private void mockCurrentUser(String username, Long userId) {
        User u = new User();
        u.setId(userId);
        u.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(u));
        TestAuthHelper.setAuth(username);
    }
}
