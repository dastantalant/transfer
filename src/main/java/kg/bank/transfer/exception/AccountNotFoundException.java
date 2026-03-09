package kg.bank.transfer.exception;

import kg.bank.transfer.model.AccountNumber;

public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(AccountNumber accountNumber) {
        super(accountNumber);
    }

    public AccountNotFoundException(AccountNumber accountNumber, String message) {
        super(accountNumber, message);
    }

    public AccountNotFoundException(AccountNumber accountNumber, String message, Throwable cause) {
        super(accountNumber, message, cause);
    }

    public AccountNotFoundException(AccountNumber accountNumber, Throwable cause) {
        super(accountNumber, cause);
    }

    public AccountNotFoundException(AccountNumber accountNumber, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(accountNumber, message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String error() {
        return "ACCOUNT_NOT_FOUND";
    }
}
