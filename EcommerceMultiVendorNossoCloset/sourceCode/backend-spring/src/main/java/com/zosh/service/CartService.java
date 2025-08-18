package com.nossocloset.service;

import com.nossocloset.exception.ProductException;
import com.nossocloset.model.Cart;
import com.nossocloset.model.CartItem;
import com.nossocloset.model.Product;
import com.nossocloset.model.User;

public interface CartService {
	
	public CartItem addCartItem(User user,
								Product product,
								String size,
								int quantity) throws ProductException;
	
	public Cart findUserCart(User user);

}
