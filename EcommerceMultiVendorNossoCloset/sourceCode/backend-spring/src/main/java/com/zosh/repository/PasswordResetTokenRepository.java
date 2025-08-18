package com.nossocloset.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nossocloset.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
	PasswordResetToken findByToken(String token);
}
