package kg.bank.transfer.controller;

import kg.bank.transfer.BaseIntegrationTest;
import kg.bank.transfer.entity.Account;
import kg.bank.transfer.entity.Entry;
import kg.bank.transfer.entity.Transaction;
import kg.bank.transfer.enums.AccountStatus;
import kg.bank.transfer.enums.EntryDirection;
import kg.bank.transfer.enums.TransactionStatus;
import kg.bank.transfer.enums.TransactionType;
import kg.bank.transfer.repository.AccountRepository;
import kg.bank.transfer.repository.EntryRepository;
import kg.bank.transfer.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountStatementTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE entries, transactions, accounts CASCADE");

        testAccount = new Account();
        testAccount.setNumber("111222333");
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setBalance(new BigDecimal("500.00"));
        testAccount = accountRepository.save(testAccount);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(new BigDecimal("500.00"));
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setToAccount("111222333");

        Entry creditEntry = new Entry();
        creditEntry.setAccount(testAccount);
        creditEntry.setAmount(new BigDecimal("500.00"));
        creditEntry.setDirection(EntryDirection.CREDIT);
        tx.addEntry(creditEntry);

        transactionRepository.save(tx);
        String nowStr = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        jdbcTemplate.update("UPDATE transactions SET created_at = CAST(? AS timestamptz) WHERE id = ?", nowStr, tx.getId());
        jdbcTemplate.update("UPDATE entries SET created_at = CAST(? AS timestamptz) WHERE transaction_id = ?", nowStr, tx.getId());
    }

    @AfterEach
    void tearDown() {
        entryRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("test SUCCESS")
    void testSuccess() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        mockMvc.perform(get("/api/accounts/{accountNumber}/statement", testAccount.getNumber())
                        .param("from", today.toString())
                        .param("to", today.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].type", is(TransactionType.DEPOSIT.name())))
                .andExpect(jsonPath("$.content[0].amount", is(500.0)))
                .andExpect(jsonPath("$.content[0].balanceAfter", is(500.0)))
                .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageable.pageSize", is(10)));
    }

    @Test
    @DisplayName("Test AccountNotFound")
    void testAccountNotFound() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        mockMvc.perform(get("/api/accounts/{accountNumber}/statement", "999999999")
                        .param("from", today.toString())
                        .param("to", today.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("ACCOUNT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Счет 999999999 не найден")));
    }

    @Test
    @DisplayName("Test Missing Request Parameters")
    void testMissingRequestParameters() throws Exception {
        mockMvc.perform(get("/api/accounts/{accountNumber}/statement", testAccount.getNumber())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test Empty Result for Future Dates")
    void testEmptyResultForFutureDates() throws Exception {
        LocalDate futureDate = LocalDate.now(ZoneOffset.UTC).plusDays(10);

        mockMvc.perform(get("/api/accounts/{accountNumber}/statement", testAccount.getNumber())
                        .param("from", futureDate.toString())
                        .param("to", futureDate.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(empty())));
    }

    @Test
    @DisplayName("Test With RichData Pagination and Rolling Balance")
    void testWithRichDataPaginationAndRollingBalance() throws Exception {
        Account acc = new Account();
        acc.setNumber("444555666");
        acc.setStatus(AccountStatus.ACTIVE);
        acc.setBalance(new BigDecimal("2000.00"));
        acc = accountRepository.save(acc);

        createHistoricalTx(acc, "2026-03-01 10:00:00+00", TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS, new BigDecimal("1000.00"), EntryDirection.CREDIT);
        createHistoricalTx(acc, "2026-03-03 12:00:00+00", TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS, new BigDecimal("200.00"), EntryDirection.DEBIT);

        createHistoricalTx(acc, "2026-03-05 10:00:00+00", TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS, new BigDecimal("500.00"), EntryDirection.CREDIT);
        createHistoricalTx(acc, "2026-03-06 15:00:00+00", TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS, new BigDecimal("100.00"), EntryDirection.DEBIT);

        createHistoricalTx(acc, "2026-03-07 09:00:00+00", TransactionType.DEPOSIT,
                TransactionStatus.FAILED, new BigDecimal("999.00"), EntryDirection.CREDIT);

        createHistoricalTx(acc, "2026-03-07 14:00:00+00", TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS, new BigDecimal("300.00"), EntryDirection.CREDIT);

        createHistoricalTx(acc, "2026-03-09 10:00:00+00", TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS, new BigDecimal("50.00"), EntryDirection.DEBIT);

        mockMvc.perform(get("/api/accounts/{accountNumber}/statement", "444555666")
                        .param("from", "2026-03-05")
                        .param("to", "2026-03-07")
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))

                // from 2026-03-07
                .andExpect(jsonPath("$.content[0].type", is(TransactionType.DEPOSIT.name())))
                .andExpect(jsonPath("$.content[0].amount", is(300.0)))
                .andExpect(jsonPath("$.content[0].balanceAfter", is(1500.0)))

                // from 2026-03-06
                .andExpect(jsonPath("$.content[1].type", is(TransactionType.WITHDRAWAL.name())))
                .andExpect(jsonPath("$.content[1].amount", is(-100.0)))
                .andExpect(jsonPath("$.content[1].balanceAfter", is(1200.0)));

        mockMvc.perform(get("/api/accounts/{accountNumber}/statement", "444555666")
                        .param("from", "2026-03-05")
                        .param("to", "2026-03-07")
                        .param("page", "1")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].type", is(TransactionType.DEPOSIT.name())))
                .andExpect(jsonPath("$.content[0].amount", is(500.0)))
                .andExpect(jsonPath("$.content[0].balanceAfter", is(1300.0)));
    }

    private void createHistoricalTx(Account acc, String createdAtStr, TransactionType txType, TransactionStatus status, BigDecimal amount, EntryDirection direction) {
        Transaction tx = new Transaction();
        tx.setType(txType);
        tx.setAmount(amount.abs());
        tx.setStatus(status);

        Entry entry = new Entry();
        entry.setAccount(acc);
        entry.setAmount(direction == EntryDirection.DEBIT ? amount.negate() : amount.abs());
        entry.setDirection(direction);
        tx.addEntry(entry);

        transactionRepository.save(tx);

        jdbcTemplate.update("UPDATE transactions SET created_at = CAST(? AS timestamptz) WHERE id = ?", createdAtStr, tx.getId());
        jdbcTemplate.update("UPDATE entries SET created_at = CAST(? AS timestamptz) WHERE transaction_id = ?", createdAtStr, tx.getId());
    }
}