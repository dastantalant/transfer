-- 1. Найти ТОП-5 счетов по количеству транзакций за последний месяц
SELECT e.account_id,
       a.number,
       COUNT(t.id) as tx_count
FROM entries e
         JOIN transactions t ON e.transaction_id = t.id
         JOIN accounts a ON e.account_id = a.id
WHERE t.created_at >= CURRENT_DATE - INTERVAL '1 month'
GROUP BY e.account_id, a.number
ORDER BY tx_count DESC
LIMIT 5;

-- 2. Посчитать общую сумму переводов за выбранный период
SELECT COALESCE(SUM(amount), 0) as total_transfer_sum
FROM transactions
WHERE type = 'TRANSFER'
  AND status = 'SUCCESS'
  AND created_at BETWEEN '2026-03-01 00:00:00' AND '2026-03-31 23:59:59';

-- 3. Найти счета с отрицательным балансом
SELECT a.number                   AS account_number,
       COALESCE(SUM(e.amount), 0) AS balance
FROM entries e
         JOIN transactions t ON t.id = e.transaction_id
         JOIN accounts a ON a.id = e.account_id
WHERE t.status = 'SUCCESS'
GROUP BY e.number
HAVING COALESCE(SUM(e.amount), 0) < 0;

-- 4. Оптимизировать один из запросов (добавить индекс и объяснить почему)
-- Запрос №2 ищет по: type = 'TRANSFER' AND status = 'SUCCESS' AND created_at BETWEEN X AND Y
-- Без составного индекса `idx_transactions_type_status_created` база делает Full Table Scan
CREATE INDEX idx_transactions_type_status_created
    ON transactions (type, status, created_at);
-- Для поиска аккаунта и защиты от дублей (учитывает Soft Delete)
CREATE UNIQUE INDEX idx_accounts_number_active
    ON accounts (number)
    WHERE deleted_at IS NULL;
-- Для поиска по счету, JOIN с транзакциями и суммирование баланса (amount)
CREATE INDEX idx_entries_account_tx_amount
    ON entries (account_id, transaction_id, amount);