package com.zosh.dto;

import com.zosh.domain.CollectiveOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respostas de CollectiveOrder
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectiveOrderResponseDto {

    private Long id;
    private SupplierSummaryDto supplier;
    private String title;
    private String description;
    private CollectiveOrderStatus status;
    private Integer minimumQuantity;
    private Integer currentQuantity;
    private BigDecimal minimumOrderValue;
    private BigDecimal currentOrderValue;
    private LocalDateTime paymentDeadline;
    private LocalDateTime fulfillmentDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Campos calculados
    private BigDecimal progressPercentage;
    private long daysUntilDeadline;
    private boolean isActive;
    private boolean isCompleted;
    private boolean isExpired;
    private boolean hasMinimumReached;
    
    // Relacionamentos
    private List<CustomOrderSummaryDto> customOrders;
    private int totalParticipants;

    // DTOs aninhados
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
    public static class CustomOrderSummaryDto {
        private Long id;
        private String clientName;
        private String description;
        private BigDecimal finalPrice;
        private Integer quantity;
        private LocalDateTime createdAt;
    }
}
