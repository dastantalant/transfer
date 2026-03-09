package kg.bank.transfer.repository;

import kg.bank.transfer.dto.AccountStatementResponse;
import kg.bank.transfer.entity.Transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query(value = """
            WITH t_start AS (
                SELECT COALESCE(SUM(e.amount),0) AS balance
                FROM entries e
                JOIN transactions t ON t.id = e.transaction_id
                WHERE e.account_id = :accountId
                  AND t.status = 'SUCCESS'
                  AND t.created_at < :from),
            t_period AS (
                SELECT
                    t.created_at AS date,
                    t.type       AS type,
                    e.amount     AS amount,
                    e.id         AS entry_id
                FROM entries e
                JOIN transactions t ON t.id = e.transaction_id
                WHERE e.account_id = :accountId
                  AND t.status = 'SUCCESS'
                  AND t.created_at BETWEEN :from AND :to)
            SELECT
                p.date AS date,
                p.type AS type,
                p.amount AS amount,
                (SELECT balance FROM t_start) + SUM(p.amount) OVER (ORDER BY p.date, p.entry_id) AS balanceAfter
            FROM t_period p
            ORDER BY p.date DESC
            """,
            countQuery = """
                    SELECT count(*)
                    FROM entries e
                    JOIN transactions t ON t.id = e.transaction_id
                    WHERE e.account_id = :accountId
                      AND t.status = 'SUCCESS'
                      AND t.created_at BETWEEN :from AND :to
                    """,
            nativeQuery = true)
    Page<AccountStatementResponse> findAllByAccountIdAndPeriod(
            @Param("accountId") Long accountId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );
}