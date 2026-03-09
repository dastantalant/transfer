package kg.bank.transfer.controller;

import kg.bank.transfer.dto.TransferRequest;
import kg.bank.transfer.service.TransactionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransactionService service;

    @PostMapping
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        service.transfer(request);
        return ResponseEntity.ok().build();
    }
}
