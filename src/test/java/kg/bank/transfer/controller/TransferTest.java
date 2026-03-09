package kg.bank.transfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.bank.transfer.BaseIntegrationTest;
import kg.bank.transfer.dto.TransferRequest;
import kg.bank.transfer.entity.Account;
import kg.bank.transfer.entity.Transaction;
import kg.bank.transfer.enums.AccountStatus;
import kg.bank.transfer.enums.TransactionStatus;
import kg.bank.transfer.repository.AccountRepository;
import kg.bank.transfer.repository.EntryRepository;
import kg.bank.transfer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE entries, transactions, accounts CASCADE");

        Account acc1 = new Account();
        acc1.setNumber("111111");
        acc1.setStatus(AccountStatus.ACTIVE);
        acc1.setBalance(new BigDecimal("1000.00"));

        Account acc2 = new Account();
        acc2.setNumber("222222");
        acc2.setStatus(AccountStatus.ACTIVE);
        acc2.setBalance(new BigDecimal("500.00"));

        accountRepository.saveAll(List.of(acc1, acc2));
    }

    @Test
    @Transactional
    @DisplayName("Test SUCCESS transfer")
    void testSuccessfulTransfer() throws Exception {
        TransferRequest request = new TransferRequest("111111", "222222", new BigDecimal("200.00"), "Оплата услуг");

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Account updatedAcc1 = accountRepository.findByNumber("111111").orElseThrow();
        Account updatedAcc2 = accountRepository.findByNumber("222222").orElseThrow();

        // 1000 - 200 = 800
        assertThat(updatedAcc1.getBalance()).isEqualByComparingTo("800.00");
        // 500 + 200 = 700
        assertThat(updatedAcc2.getBalance()).isEqualByComparingTo("700.00");

        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.getFirst().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(transactions.getFirst().getEntries()).hasSize(2);
    }

    @Test
    @DisplayName("Test FAILED transfer")
    void testInsufficientFundsTransfer() throws Exception {
        TransferRequest request = new TransferRequest("111111", "222222", new BigDecimal("2000.00"), "Оплата услуг");

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        Account updatedAcc1 = accountRepository.findByNumber("111111").orElseThrow();
        Account updatedAcc2 = accountRepository.findByNumber("222222").orElseThrow();

        assertThat(updatedAcc1.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(updatedAcc2.getBalance()).isEqualByComparingTo("500.00");

        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.getFirst().getStatus()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    @DisplayName("Test Concurrency transfer")
    void testConcurrentTransfers() throws Exception {
        int threadsCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch latch = new CountDownLatch(threadsCount);

        TransferRequest request = new TransferRequest("111111", "222222", new BigDecimal("50.00"), "Concurrent Test");
        String requestBody = objectMapper.writeValueAsString(request);

        for (int i = 0; i < threadsCount; i++) {
            executorService.execute(() -> {
                try {
                    mockMvc.perform(post("/api/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Account updatedAcc1 = accountRepository.findByNumber("111111").orElseThrow();
        Account updatedAcc2 = accountRepository.findByNumber("222222").orElseThrow();

        // balance = balance - (request.amount * thread)
        // balance = 1000.00 - (10 * 50.00)
        assertThat(updatedAcc1.getBalance()).isEqualByComparingTo("500.00");
        // balance = balance + (request.amount * thread)
        // balance = 500.00 + (10 * 50.00)
        assertThat(updatedAcc2.getBalance()).isEqualByComparingTo("1000.00");

        long successCount = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .count();
        assertThat(successCount).isEqualTo(10);
    }
}