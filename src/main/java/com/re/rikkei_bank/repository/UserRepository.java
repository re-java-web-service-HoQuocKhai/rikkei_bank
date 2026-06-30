package com.re.rikkei_bank.repository;

import com.re.rikkei_bank.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

    @Query("SELECT new com.re.rikkei_bank.dto.projection.UserProjection(" +
           "u.id, u.username, u.email, u.phoneNumber, r.name, u.isActive, u.isKyc, u.createdAt) " +
           "FROM User u " +
           "LEFT JOIN u.role r " +
           "LEFT JOIN KycProfile k ON k.user = u " +
           "WHERE (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "      OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:cccd IS NULL OR k.idNumber = :cccd) " +
           "AND (:status IS NULL OR u.isActive = :status) " +
           "AND (:roleName IS NULL OR r.name = :roleName)")
    Page<com.re.rikkei_bank.dto.projection.UserProjection> searchUsers(
            @Param("keyword") String keyword,
            @Param("cccd") String cccd,
            @Param("status") Boolean status,
            @Param("roleName") String roleName,
            Pageable pageable);
}
