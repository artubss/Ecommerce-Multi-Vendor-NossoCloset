package com.nossocloset.repository;

import com.nossocloset.model.Cart;
import com.nossocloset.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nossocloset.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {


    CartItem findByCartAndProductAndSize(Cart cart, Product product, String size);


}
