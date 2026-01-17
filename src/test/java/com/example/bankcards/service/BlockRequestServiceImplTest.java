package com.example.bankcards.service;

import com.example.bankcards.dto.BlockRequestCreateRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.ApiException;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
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
class BlockRequestServiceImplTest {

    @Mock BlockRequestRepository blockRequestRepository;
    @Mock CardRepository cardRepository;
    @Mock UserRepository userRepository;

    @InjectMocks BlockRequestServiceImpl service;

    @BeforeEach
    void setup() {
        TestAuthHelper.clear();
    }

    @Test
    void createMy_ok_createsPending() {
        User u = new User();
        u.setId(2L);
        u.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(u));

        Card card = new Card();
        card.setId(10L);
        card.setOwner(u);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryMonth(12);
        card.setExpiryYear(YearMonth.now().getYear() + 1);

        TestAuthHelper.setAuth("user");

        when(cardRepository.findByIdAndOwnerId(10L, 2L)).thenReturn(Optional.of(card));
        when(blockRequestRepository.existsByCardIdAndStatus(10L, BlockRequestStatus.PENDING)).thenReturn(false);

        when(blockRequestRepository.save(any(BlockRequest.class))).thenAnswer(inv -> {
            BlockRequest br = inv.getArgument(0, BlockRequest.class);
            br.setId(200L);
            return br;
        });

        BlockRequest br = service.createMy(new BlockRequestCreateRequest(10L));

        assertEquals(200L, br.getId());
        assertEquals(BlockRequestStatus.PENDING, br.getStatus());
        assertNotNull(br.getRequestedBy());
        assertEquals(2L, br.getRequestedBy().getId());
        verify(blockRequestRepository, times(1)).save(any(BlockRequest.class));
    }

    @Test
    void createMy_duplicatePending_conflict() {
        User u = new User();
        u.setId(2L);
        u.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(u));

        Card card = new Card();
        card.setId(10L);
        card.setOwner(u);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryMonth(12);
        card.setExpiryYear(YearMonth.now().getYear() + 1);

        TestAuthHelper.setAuth("user");

        when(cardRepository.findByIdAndOwnerId(10L, 2L)).thenReturn(Optional.of(card));
        when(blockRequestRepository.existsByCardIdAndStatus(10L, BlockRequestStatus.PENDING)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> service.createMy(new BlockRequestCreateRequest(10L)));

        assertEquals("CONFLICT", ex.getError());
        verify(blockRequestRepository, never()).save(any());
    }

    @Test
    void adminApprove_setsApprovedAndBlocksCard() {
        BlockRequest br = new BlockRequest();
        br.setId(200L);
        br.setStatus(BlockRequestStatus.PENDING);

        Card card = new Card();
        card.setId(10L);
        card.setStatus(CardStatus.ACTIVE);
        br.setCard(card);

        when(blockRequestRepository.findById(200L)).thenReturn(Optional.of(br));
        when(blockRequestRepository.save(any(BlockRequest.class))).thenAnswer(inv -> inv.getArgument(0, BlockRequest.class));

        BlockRequest result = service.adminApprove(200L);

        assertEquals(BlockRequestStatus.APPROVED, result.getStatus());
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(blockRequestRepository, times(1)).save(any(BlockRequest.class));
    }

    @Test
    void adminApprove_notPending_conflict() {
        BlockRequest br = new BlockRequest();
        br.setId(200L);
        br.setStatus(BlockRequestStatus.REJECTED);

        when(blockRequestRepository.findById(200L)).thenReturn(Optional.of(br));

        ApiException ex = assertThrows(ApiException.class, () -> service.adminApprove(200L));
        assertEquals("CONFLICT", ex.getError());
        verify(blockRequestRepository, never()).save(any());
    }
}
