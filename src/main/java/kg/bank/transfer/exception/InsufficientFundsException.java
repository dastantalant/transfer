package kg.bank.transfer.exception;

import kg.bank.transfer.model.AccountNumber;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientFundsException extends BusinessException {

    private final BigDecimal amount;

    public InsufficientFundsException(AccountNumber accountNumber, BigDecimal amount) {
        super(accountNumber);
        this.amount = amount;
    }

    public InsufficientFundsException(AccountNumber accountNumber, BigDecimal amount, String message) {
        super(accountNumber, message);
        this.amount = amount;
    }

    public InsufficientFundsException(AccountNumber accountNumber, BigDecimal amount, String message, Throwable cause) {
        super(accountNumber, message, cause);
        this.amount = amount;
    }

    public InsufficientFundsException(AccountNumber accountNumber, BigDecimal amount, Throwable cause) {
        super(accountNumber, cause);
        this.amount = amount;
    }

    public InsufficientFundsException(AccountNumber accountNumber, BigDecimal amount, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(accountNumber, message, cause, enableSuppression, writableStackTrace);
        this.amount = amount;
    }

    @Override
    public String error() {
        return "INSUFFICIENT_FUNDS";
    }
}
