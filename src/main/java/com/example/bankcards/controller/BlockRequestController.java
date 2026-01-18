package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockRequestCreateRequest;
import com.example.bankcards.dto.BlockRequestResponse;
import com.example.bankcards.service.BlockRequestService;
import com.example.bankcards.util.BlockRequestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Block Requests")
@RestController
@RequestMapping("/api/block-requests")
public class BlockRequestController {

    private final BlockRequestService blockRequests;

    public BlockRequestController(BlockRequestService blockRequests) {
        this.blockRequests = blockRequests;
    }

    @Operation(summary = "Create block request for own card")
    @PostMapping
    public BlockRequestResponse create(@Valid @RequestBody BlockRequestCreateRequest req) {
        return BlockRequestMapper.toResponse(blockRequests.createMy(req));
    }

    @Operation(summary = "List own block requests")
    @GetMapping("/my")
    public Page<BlockRequestResponse> my(@ParameterObject Pageable pageable) {
        return blockRequests.myRequests(pageable).map(BlockRequestMapper::toResponse);
    }
}
