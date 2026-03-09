package kg.bank.transfer.dto;

import kg.bank.transfer.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public interface AccountStatementResponse {
    Instant getDate();

    TransactionType getType();

    BigDecimal getAmount();

    BigDecimal getBalanceAfter();
}