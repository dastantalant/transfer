package kg.bank.transfer.exception;

import kg.bank.transfer.model.AccountNumber;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {
    private final transient AccountNumber accountNumber;

    protected BusinessException(AccountNumber accountNumber) {
        this.accountNumber = accountNumber;
    }

    protected BusinessException(AccountNumber accountNumber, String message) {
        super(message);
        this.accountNumber = accountNumber;
    }

    protected BusinessException(AccountNumber accountNumber, String message, Throwable cause) {
        super(message, cause);
        this.accountNumber = accountNumber;
    }

    protected BusinessException(AccountNumber accountNumber, Throwable cause) {
        super(cause);
        this.accountNumber = accountNumber;
    }

    protected BusinessException(AccountNumber accountNumber, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.accountNumber = accountNumber;
    }

    public abstract String error();

}
