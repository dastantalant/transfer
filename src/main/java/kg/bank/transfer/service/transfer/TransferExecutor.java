package kg.bank.transfer.service.transfer;

import kg.bank.transfer.dto.TransferRequest;
import kg.bank.transfer.entity.Account;
import kg.bank.transfer.entity.Entry;
import kg.bank.transfer.entity.Transaction;
import kg.bank.transfer.enums.EntryDirection;
import kg.bank.transfer.enums.TransactionStatus;
import kg.bank.transfer.exception.AccountNotFoundException;
import kg.bank.transfer.exception.SelfTransferException;
import kg.bank.transfer.model.AccountNumber;
import kg.bank.transfer.repository.AccountRepository;
import kg.bank.transfer.repository.TransactionRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferExecutor {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final List<PreTransferProcessing> processors;

    @Transactional
    public void execute(TransferRequest request, Transaction tx) {
        UUID txId = tx.getId();
        log.debug("Transaction[{}]: Execution started. Initializing validation and lock procedures.", txId);
        if (request.from().equals(request.to())) {
            log.warn("Transaction [{}]: Attempted self-transfer on account '{}'", txId, request.from());
            throw new SelfTransferException(new AccountNumber(request.from()));
        }

        log.debug("Transaction [{}]: Sorting accounts to prevent DB deadlocks", txId);
        boolean compare = request.from().compareTo(request.to()) < 0;
        String first = compare ? request.from() : request.to();
        String second = compare ? request.to() : request.from();

        log.debug("Transaction [{}]: Acquiring pessimistic write locks for accounts '{}' and '{}'", txId, first, second);
        Account firstAccount = accountRepository.findByNumberForUpdate(first)
                .orElseThrow(() -> {
                    log.error("Transaction [{}]: Lock failed. Account '{}' not found", txId, first);
                    return new AccountNotFoundException(new AccountNumber(first));
                });
        Account secondAccount = accountRepository.findByNumberForUpdate(second)
                .orElseThrow(() -> {
                    log.error("Transaction [{}]: Lock failed. Account '{}' not found", txId, second);
                    return new AccountNotFoundException(new AccountNumber(second));
                });

        Account source = firstAccount.getNumber().equals(request.from()) ? firstAccount : secondAccount;
        Account target = firstAccount.getNumber().equals(request.to()) ? firstAccount : secondAccount;

        log.debug("Transaction [{}]: Executing pre-transfer processors (count: {})", txId, processors.size());
        for(PreTransferProcessing processor : processors) {
            processor.process(source,target,request, txId);
        }

        BigDecimal amount = request.amount();
        log.debug("Transaction [{}]: Updating balances. Deducting {} from '{}', adding to '{}'",
                txId, amount, source.getNumber(), target.getNumber());

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        log.debug("Transaction [{}]: Creating double-entry accounting records", txId);
        Entry debit = new Entry();
        debit.setTransaction(tx);
        debit.setAccount(source);
        debit.setAmount(amount.negate());
        debit.setDirection(EntryDirection.DEBIT);

        Entry credit = new Entry();
        credit.setTransaction(tx);
        credit.setAccount(target);
        credit.setAmount(amount);
        credit.setDirection(EntryDirection.CREDIT);

        tx.addEntry(debit);
        tx.addEntry(credit);

        log.debug("Transaction [{}]: Successfully prepared. Saving to database", txId);
        tx.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(tx);
    }
}