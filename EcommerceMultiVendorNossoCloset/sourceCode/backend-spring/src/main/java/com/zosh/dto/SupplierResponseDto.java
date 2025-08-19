package com.zosh.dto;

import com.zosh.domain.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respostas de Supplier
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDto {

    private Long id;
    private String name;
    private String contactName;
    private String whatsapp;
    private String website;
    private String email;
    private BigDecimal minimumOrderValue;
    private Integer deliveryTimeDays;
    private BigDecimal adminFeePercentage;
    private List<String> categories;
    private Integer performanceRating;
    private SupplierStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Campos calculados
    private int catalogCount;
    private int activeOrdersCount;
    private BigDecimal totalOrdersValue;
    private boolean isActive;
}
