package com.re.rikkei_bank.repository;

import com.re.rikkei_bank.model.KycProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.re.rikkei_bank.model.KycStatus;

import java.util.Optional;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {
    Optional<KycProfile> findByIdNumber(String idNumber);
    boolean existsByIdNumber(String idNumber);

    @Query("SELECT k FROM KycProfile k WHERE " +
           "(:status IS NULL OR k.status = :status) AND " +
           "(:keyword IS NULL OR k.idNumber LIKE %:keyword% OR LOWER(k.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KycProfile> searchKycProfiles(@Param("status") KycStatus status, @Param("keyword") String keyword, Pageable pageable);
}
