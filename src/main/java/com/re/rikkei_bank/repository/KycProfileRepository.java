package com.re.rikkei_bank.repository;

import com.re.rikkei_bank.model.KycProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {
    Optional<KycProfile> findByIdNumber(String idNumber);
    boolean existsByIdNumber(String idNumber);
}
