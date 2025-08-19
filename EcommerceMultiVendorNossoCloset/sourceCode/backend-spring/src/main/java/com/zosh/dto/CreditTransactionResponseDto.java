package com.zosh.dto;

import com.zosh.domain.CreditStatus;
import com.zosh.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respostas de CreditTransaction
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionResponseDto {

    private Long id;
    private UserSummaryDto user;
    private BigDecimal amount;
    private TransactionType type;
    private CreditStatus status;
    private String description;
    private String notes;
    private LocalDateTime transactionDate;
    private LocalDateTime processedAt;
    private String processedBy;
    private OrderSummaryDto relatedOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Campos calculados
    private BigDecimal balanceAfter;
    private boolean isCredit;
    private boolean isDebit;
    private boolean isPending;
    private boolean isCompleted;
    private String formattedAmount;

    // DTOs aninhados
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDto {
        private Long id;
        private String fullName;
        private String email;
        private BigDecimal currentBalance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryDto {
        private Long id;
        private String description;
        private BigDecimal totalValue;
        private String status;
    }
}
