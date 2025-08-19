package com.zosh.dto;

import com.zosh.domain.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para requisições de CreditTransaction
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionRequestDto {

    @NotNull(message = "ID do usuário é obrigatório")
    private Long userId;

    @NotNull(message = "Valor da transação é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    @NotNull(message = "Tipo da transação é obrigatório")
    private TransactionType type;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 5, max = 200, message = "Descrição deve ter entre 5 e 200 caracteres")
    private String description;

    private Long relatedOrderId;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String notes;
}
