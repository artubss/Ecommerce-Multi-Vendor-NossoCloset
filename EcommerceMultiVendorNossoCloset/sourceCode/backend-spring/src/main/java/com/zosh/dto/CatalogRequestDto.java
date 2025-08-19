package com.zosh.dto;

import com.zosh.domain.CatalogType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para requisições de Catalog
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogRequestDto {

    @NotNull(message = "ID do fornecedor é obrigatório")
    private Long supplierId;

    @NotBlank(message = "Nome do catálogo é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 500, message = "Descrição deve ter entre 10 e 500 caracteres")
    private String description;

    @Size(max = 500, message = "URL do arquivo deve ter no máximo 500 caracteres")
    private String fileUrl;

    private List<String> imageUrls;

    private CatalogType type = CatalogType.PDF;

    @NotNull(message = "Data de início de validade é obrigatória")
    @Future(message = "Data de início deve ser no futuro")
    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    @Size(max = 50, message = "Temporada deve ter no máximo 50 caracteres")
    private String season;

    private List<String> tags;
}
