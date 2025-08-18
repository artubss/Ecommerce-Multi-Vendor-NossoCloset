package com.nossocloset.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nossocloset.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

	 Cart findByUserId(Long userId);
}
