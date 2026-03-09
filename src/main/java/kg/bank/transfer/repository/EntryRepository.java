package kg.bank.transfer.repository;

import kg.bank.transfer.entity.Entry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Entry e WHERE e.account.id = :accountId")
    BigDecimal sumByAccountId(Long accountId);

}
