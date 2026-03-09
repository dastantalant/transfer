package kg.bank.transfer.exception;

import kg.bank.transfer.model.AccountNumber;

public class SelfTransferException extends BusinessException {
    public SelfTransferException(AccountNumber accountNumber) {
        super(accountNumber);
    }

    public SelfTransferException(AccountNumber accountNumber, String message) {
        super(accountNumber, message);
    }

    public SelfTransferException(AccountNumber accountNumber, String message, Throwable cause) {
        super(accountNumber, message, cause);
    }

    public SelfTransferException(AccountNumber accountNumber, Throwable cause) {
        super(accountNumber, cause);
    }

    public SelfTransferException(AccountNumber accountNumber, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(accountNumber, message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String error() {
        return "SELF_TRANSFER";
    }
}
