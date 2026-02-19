package org.ft.bank.service;

import lombok.RequiredArgsConstructor;
import org.ft.bank.dto.BankSuccessEvent;
import org.ft.bank.entity.Account;
import org.ft.bank.entity.BankIdempotency;
import org.ft.bank.publisher.BankEventPublisher;
import org.ft.bank.repository.AccountRepository;
import org.ft.bank.repository.BankIdempotencyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankService {

    private final AccountRepository accountRepo;
    private final BankIdempotencyRepository idemRepo;
    private final BankEventPublisher eventPublisher;
    @Value("${bank.default-currency}")
    private String defaultCurrency;
    @Value("${fintech.idempotency.key-prefix}")
    private String keyPrefix;

    @Transactional
    public boolean debit(
            String customerId,
            BigDecimal amount,
            String currency,
            String clientProvidedKey
    ) {
        String bankIdempotencyKey = keyPrefix + "-" + clientProvidedKey;


        // Idempotency Check
        Optional<BankIdempotency> existing = idemRepo.findById(bankIdempotencyKey);
        if (existing.isPresent()) {
            if ("SUCCESS".equals(existing.get().getStatus())) return true;
            if ("PROCESSING".equals(existing.get().getStatus())) throw new RuntimeException("Duplicate in progress");
            if ("FAILED".equals(existing.get().getStatus())) return false;
        }

        //Save PROCESSING
        BankIdempotency idem = BankIdempotency.builder()
                .idempotencyKey(bankIdempotencyKey)
                .transactionId(UUID.randomUUID())
                .status("PROCESSING")
                .createdAt(LocalDateTime.now())
                .build();
        idemRepo.save(idem);

        //Debit Logic
        Account account = createAccountIfNotExists(customerId);

        if (account.getBalance().compareTo(amount) < 0) {
            idem.setStatus("FAILED");
            idemRepo.save(idem);
            return false;
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepo.save(account);

        //Publish Success Event
        BankSuccessEvent event = BankSuccessEvent.builder()
                .eventId(UUID.randomUUID())
                .transactionId(idem.getTransactionId())
                .customerId(customerId)
                .amount(amount)
                .currency(currency)
                .idempotencyKey(bankIdempotencyKey)
                .build();

        eventPublisher.publishBankSuccess(event);

        //Mark SUCCESS
        idem.setStatus("SUCCESS");
        idemRepo.save(idem);
        return  true;
    }

    public Account  createAccountIfNotExists(String customerId) {
      return accountRepo.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Account acc = new Account();
                    acc.setCustomerId(customerId);
                    acc.setCurrency(defaultCurrency);
                    acc.setBalance(BigDecimal.ZERO);
                    return accountRepo.save(acc);
                });
    }
}
