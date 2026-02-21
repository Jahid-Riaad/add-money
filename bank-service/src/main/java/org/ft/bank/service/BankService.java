package org.ft.bank.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;
import org.ft.bank.dto.BankSuccessEvent;
import org.ft.bank.dto.TransferEvent;
import org.ft.bank.entity.Account;
import org.ft.bank.entity.TransactionLog;
import org.ft.bank.enumeration.IdempotencyStatus;
import org.ft.bank.publisher.BankEventPublisher;
import org.ft.bank.repository.AccountRepository;
import org.ft.bank.repository.TransactionLogRepository;
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
    private final TransactionLogRepository idemRepo;
    private final BankEventPublisher eventPublisher;
    @Value("${bank.default-currency}")
    private String defaultCurrency;

    @Transactional
    public boolean debit(TransferEvent transferEvent) {
        // 1. Idempotency Check
        Optional<TransactionLog> existing = idemRepo.findBySourceAndIdempotencyKeyAndTransactionId(
                transferEvent.getSource(),
                transferEvent.getIdempotencyKey(),
                transferEvent.getTransactionId()
        );

        if (existing.isPresent()) {
            TransactionLog record = existing.get();
            return switch (record.getStatus()) {
                case SUCCESS -> true;
                case FAILED -> false;
                case PROCESSING -> throw new RuntimeException("Transaction currently being processed");
            };
        }

        //Save Initial State
        TransactionLog idem = TransactionLog.builder()
                .idempotencyKey(transferEvent.getIdempotencyKey())
                .transactionId(transferEvent.getTransactionId())
                .source(transferEvent.getSource())
                .status(IdempotencyStatus.PROCESSING)
                .amount(transferEvent.getAmount())
                .createdAt(LocalDateTime.now())
                .build();
        idemRepo.save(idem);

        //Balance Debit Logic
        //Creating account to continue
        Account account = createAccountIfNotExists(transferEvent.getCustomerId());

        if (account.getBalance().compareTo(transferEvent.getAmount()) < 0) {
            idem.setStatus(IdempotencyStatus.FAILED);
            //we can publish a BankFailureEvent here
            return false;
        }

        account.setBalance(account.getBalance().subtract(transferEvent.getAmount()));
        accountRepo.save(account);

        // Publish Success Event
        BankSuccessEvent event = BankSuccessEvent.builder()
                .eventId(UUID.randomUUID())
                .idempotencyKey(transferEvent.getIdempotencyKey())
                .transactionId(transferEvent.getTransactionId())
                .customerId(transferEvent.getCustomerId())
                .amount(transferEvent.getAmount())
                .currency(transferEvent.getCurrency())
                .build();

        eventPublisher.publishBankSuccess(event);

        // 5. Mark SUCCESS
        idem.setStatus(IdempotencyStatus.SUCCESS);
        return true;
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
