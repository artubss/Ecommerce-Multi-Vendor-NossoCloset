package com.zosh.dto;

import com.zosh.domain.UrgencyLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para requisições de CustomOrder
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomOrderRequestDto {

    @NotNull(message = "ID do cliente é obrigatório")
    private Long clientId;

    @NotBlank(message = "URL da imagem do produto é obrigatória")
    @Size(max = 500, message = "URL da imagem deve ter no máximo 500 caracteres")
    private String productImageUrl;

    @Size(max = 50, message = "Código de referência deve ter no máximo 50 caracteres")
    private String referenceCode;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 1000, message = "Descrição deve ter entre 10 e 1000 caracteres")
    private String description;

    @NotBlank(message = "Cor preferencial é obrigatória")
    @Size(max = 50, message = "Cor preferencial deve ter no máximo 50 caracteres")
    private String preferredColor;

    private List<String> alternativeColors;

    @NotBlank(message = "Tamanho é obrigatório")
    @Size(max = 30, message = "Tamanho deve ter no máximo 30 caracteres")
    private String size;

    @NotBlank(message = "Categoria é obrigatória")
    @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
    private String category;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observations;

    @DecimalMin(value = "0.00", message = "Preço estimado não pode ser negativo")
    private BigDecimal estimatedPrice;

    private UrgencyLevel urgency = UrgencyLevel.NORMAL;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Max(value = 10, message = "Quantidade máxima é 10 por pedido")
    private Integer quantity = 1;
}
