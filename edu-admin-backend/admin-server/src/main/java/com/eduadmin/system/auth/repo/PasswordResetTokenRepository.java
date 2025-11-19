package com.eduadmin.system.auth.repo;

import com.eduadmin.system.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByUsernameAndCodeAndUsedFalseAndExpireAtAfter(String username, String code, Instant now);
}