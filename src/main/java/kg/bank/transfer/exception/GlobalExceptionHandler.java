package kg.bank.transfer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse accountNotFoundException(AccountNotFoundException ex, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .path(extractPath(request))
                .message("Счет " + ex.getAccountNumber() + " не найден")
                .error(ex.error())
                .build();
    }

    @ExceptionHandler(InactiveAccountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse inactiveAccountException(InactiveAccountException ex, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(extractPath(request))
                .message("Счет " + ex.getAccountNumber() + " не активен")
                .error(ex.error())
                .build();
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse insufficientFunds(InsufficientFundsException ex, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(extractPath(request))
                .message("Не достаточно средств в счете " + ex.getAccountNumber() + " для перевода " + ex.getAmount())
                .error(ex.error())
                .build();
    }

    @ExceptionHandler(SelfTransferException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse selfTransferException(SelfTransferException ex, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(extractPath(request))
                .message("Нельзя перевести сам себе " + ex.getAccountNumber())
                .error(ex.error())
                .build();
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
