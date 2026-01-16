package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.BlockRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockRequestRepository extends JpaRepository<BlockRequest, Long> {

    Optional<BlockRequest> findTopByCardIdOrderByIdDesc(Long cardId);

    boolean existsByCardIdAndStatus(Long cardId, BlockRequestStatus status);
}
