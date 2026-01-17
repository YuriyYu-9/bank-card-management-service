package com.example.bankcards.service;

import com.example.bankcards.dto.TransferCreateRequest;
import com.example.bankcards.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferService {

    Transfer createMyTransfer(TransferCreateRequest req);

    Page<Transfer> myTransfers(Pageable pageable);
}
