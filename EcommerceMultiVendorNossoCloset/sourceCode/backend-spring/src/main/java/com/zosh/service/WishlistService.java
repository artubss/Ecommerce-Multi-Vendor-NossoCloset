package com.nossocloset.service;


import com.nossocloset.exception.WishlistNotFoundException;
import com.nossocloset.model.Product;
import com.nossocloset.model.User;
import com.nossocloset.model.Wishlist;

public interface WishlistService {

    Wishlist createWishlist(User user);

    Wishlist getWishlistByUserId(User user);

    Wishlist addProductToWishlist(User user, Product product) throws WishlistNotFoundException;

}

