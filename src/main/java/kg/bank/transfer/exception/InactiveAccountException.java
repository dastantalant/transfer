package kg.bank.transfer.exception;

import kg.bank.transfer.model.AccountNumber;

public class InactiveAccountException extends BusinessException {
    public InactiveAccountException(AccountNumber accountNumber) {
        super(accountNumber);
    }

    public InactiveAccountException(AccountNumber accountNumber, String message) {
        super(accountNumber, message);
    }

    public InactiveAccountException(AccountNumber accountNumber, String message, Throwable cause) {
        super(accountNumber, message, cause);
    }

    public InactiveAccountException(AccountNumber accountNumber, Throwable cause) {
        super(accountNumber, cause);
    }

    public InactiveAccountException(AccountNumber accountNumber, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(accountNumber, message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String error() {
        return "INACTIVE_ACCOUNT";
    }
}
