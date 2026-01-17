package com.example.bankcards.service;

import com.example.bankcards.dto.BlockRequestCreateRequest;
import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.BlockRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlockRequestService {

    BlockRequest createMy(BlockRequestCreateRequest req);

    Page<BlockRequest> myRequests(Pageable pageable);

    Page<BlockRequest> adminList(Pageable pageable, BlockRequestStatus status, Long cardId);

    BlockRequest adminApprove(Long requestId);

    BlockRequest adminReject(Long requestId);
}
