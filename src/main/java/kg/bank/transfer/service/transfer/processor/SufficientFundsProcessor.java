package kg.bank.transfer.service.transfer.processor;

import kg.bank.transfer.dto.TransferRequest;
import kg.bank.transfer.entity.Account;
import kg.bank.transfer.exception.InsufficientFundsException;
import kg.bank.transfer.model.AccountNumber;
import kg.bank.transfer.service.transfer.PreTransferProcessing;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Component
@Order(2)
public class SufficientFundsProcessor implements PreTransferProcessing {
    @Override
    public void process(Account source, Account target, TransferRequest request, UUID txId) {
        log.debug("Transaction [{}]: Checking sufficient funds for source account '{}'", txId, source.getNumber());

        if (source.getBalance().compareTo(request.amount()) < 0) {
            log.warn("Transaction [{}]: Insufficient funds in account '{}'. Required: {}, Available: {}",
                    txId, source.getNumber(), request.amount(), source.getBalance());
            throw new InsufficientFundsException(new AccountNumber(source.getNumber()), request.amount());
        }

        log.debug("Transaction [{}]: Source account has sufficient funds", txId);
    }
}