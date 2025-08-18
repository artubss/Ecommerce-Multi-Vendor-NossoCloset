package com.nossocloset.service;

import com.nossocloset.exception.ReviewNotFoundException;
import com.nossocloset.model.Product;
import com.nossocloset.model.Review;
import com.nossocloset.model.User;
import com.nossocloset.request.CreateReviewRequest;

import javax.naming.AuthenticationException;
import java.util.List;

public interface ReviewService {

    Review createReview(CreateReviewRequest req,
                        User user,
                        Product product);

    List<Review> getReviewsByProductId(Long productId);

    Review updateReview(Long reviewId,
                        String reviewText,
                        double rating,
                        Long userId) throws ReviewNotFoundException, AuthenticationException;


    void deleteReview(Long reviewId, Long userId) throws ReviewNotFoundException, AuthenticationException;

}
