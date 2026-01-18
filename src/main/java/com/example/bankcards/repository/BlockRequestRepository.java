package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.BlockRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockRequestRepository extends JpaRepository<BlockRequest, Long> {

    Optional<BlockRequest> findTopByCardIdOrderByIdDesc(Long cardId);

    boolean existsByCardIdAndStatus(Long cardId, BlockRequestStatus status);

    Page<BlockRequest> findByRequestedById(Long requestedById, Pageable pageable);

    Page<BlockRequest> findByStatus(BlockRequestStatus status, Pageable pageable);

    Page<BlockRequest> findByCardId(Long cardId, Pageable pageable);

    Page<BlockRequest> findByStatusAndCardId(BlockRequestStatus status, Long cardId, Pageable pageable);
}