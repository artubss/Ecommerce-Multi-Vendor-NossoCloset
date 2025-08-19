package com.zosh.dto;

import com.zosh.domain.CatalogStatus;
import com.zosh.domain.CatalogType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respostas de Catalog
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResponseDto {

    private Long id;
    private SupplierSummaryDto supplier;
    private String name;
    private String description;
    private String fileUrl;
    private List<String> imageUrls;
    private CatalogType type;
    private CatalogStatus status;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String season;
    private List<String> tags;
    private Integer viewCount;
    private Integer downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Campos calculados
    private int productCount;
    private boolean isActive;
    private boolean isValid;
    private boolean hasContent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierSummaryDto {
        private Long id;
        private String name;
        private String contactName;
    }
}
