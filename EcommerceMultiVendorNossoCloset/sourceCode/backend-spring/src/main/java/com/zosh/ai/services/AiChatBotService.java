package com.nossocloset.ai.services;

import com.nossocloset.exception.ProductException;
import com.nossocloset.response.ApiResponse;

public interface AiChatBotService {

    ApiResponse aiChatBot(String prompt,Long productId,Long userId) throws ProductException;
}
