package com.zosh.dto;

import com.zosh.domain.CustomOrderStatus;
import com.zosh.domain.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respostas de CustomOrder
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomOrderResponseDto {

    private Long id;
    private UserSummaryDto client;
    private SupplierSummaryDto supplier;
    private CollectiveOrderSummaryDto collectiveOrder;
    private String productImageUrl;
    private String referenceCode;
    private String description;
    private String preferredColor;
    private List<String> alternativeColors;
    private String size;
    private String category;
    private String observations;
    private BigDecimal estimatedPrice;
    private BigDecimal finalPrice;
    private CustomOrderStatus status;
    private UrgencyLevel urgency;
    private Integer quantity;
    private String adminNotes;
    private String analyzedBy;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime analyzedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime deliveredAt;
    private String createdBy;
    private String updatedBy;

    // Campos calculados
    private BigDecimal totalValue;
    private long daysOld;
    private boolean isUrgent;
    private boolean isPending;
    private boolean isConfirmed;
    private boolean isCancelled;
    private boolean isDelivered;

    // DTOs aninhados
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDto {
        private Long id;
        private String fullName;
        private String email;
        private String whatsapp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierSummaryDto {
        private Long id;
        private String name;
        private String contactName;
        private Integer deliveryTimeDays;
        private BigDecimal adminFeePercentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectiveOrderSummaryDto {
        private Long id;
        private BigDecimal minimumValue;
        private BigDecimal currentValue;
        private LocalDateTime paymentDeadline;
    }
}
