package kg.bank.transfer.service.transfer.processor;

import kg.bank.transfer.dto.TransferRequest;
import kg.bank.transfer.entity.Account;
import kg.bank.transfer.enums.AccountStatus;
import kg.bank.transfer.exception.InactiveAccountException;
import kg.bank.transfer.model.AccountNumber;
import kg.bank.transfer.service.transfer.PreTransferProcessing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class AccountStatusProcessor implements PreTransferProcessing {

    @Override
    public void process(Account source, Account target, TransferRequest request, UUID txId) {
        log.debug("Transaction[{}]: Validating account statuses", txId);

        if (source.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Transaction [{}]: Source account '{}' is inactive (Current status: {})",
                    txId, source.getNumber(), source.getStatus());
            throw new InactiveAccountException(new AccountNumber(source.getNumber()));
        }

        if (target.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Transaction [{}]: Target account '{}' is inactive (Current status: {})",
                    txId, target.getNumber(), target.getStatus());
            throw new InactiveAccountException(new AccountNumber(target.getNumber()));
        }

        log.debug("Transaction [{}]: Account statuses are valid (Both ACTIVE)", txId);
    }
}