package kg.bank.transfer.controller;

import kg.bank.transfer.dto.AccountStatementResponse;
import kg.bank.transfer.service.TransactionService;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final TransactionService transactionService;

    @GetMapping("/{accountNumber}/statement")
    public Page<AccountStatementResponse> getAccountStatement(
            @PathVariable
            String accountNumber,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam
            @Min(value = 0, message = "Page number must be >= 0")
            int page,

            @RequestParam
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size cannot exceed 100")
            int size
    ) {
        return transactionService.findAllStatementByAccountAndPeriod(accountNumber, from, to, page, size);
    }
}
