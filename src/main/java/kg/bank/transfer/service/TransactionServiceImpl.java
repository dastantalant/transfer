package kg.bank.transfer.service;

import kg.bank.transfer.dto.AccountStatementResponse;
import kg.bank.transfer.dto.TransferRequest;
import kg.bank.transfer.entity.Transaction;
import kg.bank.transfer.enums.TransactionStatus;
import kg.bank.transfer.enums.TransactionType;
import kg.bank.transfer.exception.AccountNotFoundException;
import kg.bank.transfer.exception.BusinessException;
import kg.bank.transfer.model.AccountNumber;
import kg.bank.transfer.repository.AccountRepository;
import kg.bank.transfer.repository.TransactionRepository;
import kg.bank.transfer.service.transfer.TransferExecutor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;
    private final TransferExecutor transferExecutor;

    @Override
    public void transfer(TransferRequest request) {
        Transaction tx = new Transaction();
        tx.setType(TransactionType.TRANSFER);
        tx.setAmount(request.amount());
        tx.setFromAccount(request.from());
        tx.setToAccount(request.to());

        log.info("Initiating transfer process. Transaction ID: [{}]. Details: from '{}' to '{}', amount: {}",
                tx.getId(), request.from(), request.to(), request.amount());

        try {
            transferExecutor.execute(request, tx);
            log.info("SUCCESS Transaction ID:[{}]", tx.getId());
        } catch (BusinessException ex) {
            log.warn("FAILED Transaction ID [{}] Business validation failed. {}", tx.getId(), ex.getMessage());
            tx.setStatus(TransactionStatus.FAILED);
            repository.save(tx);
            throw ex;
        } catch (Exception ex) {
            log.error("ERROR Transaction ID [{}] Unexpected critical error occurred during . Error: {}",
                    tx.getId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountStatementResponse> findAllStatementByAccountAndPeriod(String accountNumber, LocalDate from, LocalDate to, int page, int size) {
        Optional<Long> optional = accountRepository.findIdByNumber(accountNumber);
        if (optional.isEmpty()) {
            throw new AccountNotFoundException(new AccountNumber(accountNumber));
        }
        OffsetDateTime fromTime = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toTime = to.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAllByAccountIdAndPeriod(optional.get(), fromTime, toTime, pageable);
    }

}
