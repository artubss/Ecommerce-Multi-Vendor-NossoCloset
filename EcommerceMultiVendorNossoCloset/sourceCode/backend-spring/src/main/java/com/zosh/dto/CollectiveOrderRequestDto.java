package com.zosh.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para requisições de CollectiveOrder
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectiveOrderRequestDto {

    @NotNull(message = "ID do fornecedor é obrigatório")
    private Long supplierId;

    @NotNull(message = "Quantidade mínima é obrigatória")
    @Min(value = 1, message = "Quantidade mínima deve ser pelo menos 1")
    private Integer minimumQuantity;

    @NotNull(message = "Valor mínimo do pedido é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor mínimo deve ser maior que zero")
    private BigDecimal minimumOrderValue;

    @NotNull(message = "Prazo limite é obrigatório")
    @Future(message = "Prazo limite deve ser no futuro")
    private LocalDateTime paymentDeadline;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String notes;

    @Size(max = 100, message = "Título deve ter no máximo 100 caracteres")
    private String title;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;
}
