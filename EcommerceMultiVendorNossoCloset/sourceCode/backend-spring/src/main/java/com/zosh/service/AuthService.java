package com.nossocloset.service;

import com.nossocloset.exception.SellerException;
import com.nossocloset.exception.UserException;
import com.nossocloset.request.LoginRequest;
import com.nossocloset.request.SignupRequest;
import com.nossocloset.response.AuthResponse;
import jakarta.mail.MessagingException;

public interface AuthService {

    void sentLoginOtp(String email) throws UserException, MessagingException;
    String createUser(SignupRequest req) throws SellerException;
    AuthResponse signin(LoginRequest req) throws SellerException;

}
