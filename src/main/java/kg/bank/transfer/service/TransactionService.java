package kg.bank.transfer.service;

import kg.bank.transfer.dto.AccountStatementResponse;
import kg.bank.transfer.dto.TransferRequest;

import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface TransactionService {
    void transfer(TransferRequest request);

    Page<AccountStatementResponse> findAllStatementByAccountAndPeriod(String accountNumber, LocalDate from, LocalDate to, int page, int size);
}
