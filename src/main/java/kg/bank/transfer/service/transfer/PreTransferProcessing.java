package kg.bank.transfer.service.transfer;

import kg.bank.transfer.dto.TransferRequest;
import kg.bank.transfer.entity.Account;

import java.util.UUID;

@FunctionalInterface
public interface PreTransferProcessing {
    void process(Account source, Account target, TransferRequest request, UUID txId);
}
