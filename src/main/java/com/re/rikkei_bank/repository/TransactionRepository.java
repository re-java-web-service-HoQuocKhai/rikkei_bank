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

    @Query("SELECT t FROM Transaction t " +
           "WHERE (:accountId = t.fromAccount.id OR :accountId = t.toAccount.id) " +
           "AND (:type IS NULL OR (:type = 'DEBIT' AND t.fromAccount.id = :accountId) OR (:type = 'CREDIT' AND t.toAccount.id = :accountId)) " +
           "AND (cast(:startDate as timestamp) IS NULL OR t.createdAt >= :startDate) " +
           "AND (cast(:endDate as timestamp) IS NULL OR t.createdAt <= :endDate)")
    org.springframework.data.domain.Page<Transaction> getTransactionHistory(
            @Param("accountId") Long accountId,
            @Param("type") String type,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);
}
