package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockRequestResponse;
import com.example.bankcards.entity.BlockRequestStatus;
import com.example.bankcards.service.BlockRequestService;
import com.example.bankcards.util.BlockRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/block-requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlockRequestController {

    private final BlockRequestService blockRequests;

    public AdminBlockRequestController(BlockRequestService blockRequests) {
        this.blockRequests = blockRequests;
    }

    @GetMapping
    public Page<BlockRequestResponse> list(
            Pageable pageable,
            @RequestParam(required = false) BlockRequestStatus status,
            @RequestParam(required = false) Long cardId
    ) {
        return blockRequests.adminList(pageable, status, cardId).map(BlockRequestMapper::toResponse);
    }

    @PatchMapping("/{id}/approve")
    public BlockRequestResponse approve(@PathVariable Long id) {
        return BlockRequestMapper.toResponse(blockRequests.adminApprove(id));
    }

    @PatchMapping("/{id}/reject")
    public BlockRequestResponse reject(@PathVariable Long id) {
        return BlockRequestMapper.toResponse(blockRequests.adminReject(id));
    }
}
