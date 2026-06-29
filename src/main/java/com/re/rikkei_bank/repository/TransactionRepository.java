package com.re.rikkei_bank.repository;

import com.re.rikkei_bank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionCode(String transactionCode);

    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId")
    boolean hasTransactionsByUserId(@Param("userId") Long userId);
}
