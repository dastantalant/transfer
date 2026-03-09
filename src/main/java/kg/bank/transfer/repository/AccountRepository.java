package kg.bank.transfer.repository;

import kg.bank.transfer.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a.id FROM Account a WHERE a.number = :number")
    Optional<Long> findIdByNumber(@Param("number") String number);

    @Query("SELECT a FROM Account a WHERE a.number = :number")
    Optional<Account> findByNumber(@Param("number") String number);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.number = :number")
    Optional<Account> findByNumberForUpdate(String number);
}
