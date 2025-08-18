package com.nossocloset.service;

import com.nossocloset.model.VerificationCode;

public interface VerificationService {

    VerificationCode createVerificationCode(String otp, String email);
}
