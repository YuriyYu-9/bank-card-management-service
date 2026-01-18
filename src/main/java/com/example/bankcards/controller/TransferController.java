package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferCreateRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.util.TransferMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transfers")
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transfers;

    public TransferController(TransferService transfers) {
        this.transfers = transfers;
    }

    @Operation(summary = "Create transfer between own cards")
    @PostMapping
    public TransferResponse create(@Valid @RequestBody TransferCreateRequest req) {
        return TransferMapper.toResponse(transfers.createMyTransfer(req));
    }

    @Operation(summary = "List own transfers")
    @GetMapping("/my")
    public Page<TransferResponse> my(@ParameterObject Pageable pageable) {
        return transfers.myTransfers(pageable).map(TransferMapper::toResponse);
    }
}
