package org.ft.wallet.controller;

import lombok.RequiredArgsConstructor;
import org.ft.wallet.dto.TransferRequest;
import org.ft.wallet.dto.TransferResponse;
import org.ft.wallet.service.WalletService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/topup")
    public TransferResponse topup(
            @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") String key
    ) {
        return walletService.initiateTopup(request, key);
    }
}
