package com.zosh.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para requisições de Supplier
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequestDto {

    @NotBlank(message = "Nome do fornecedor é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotBlank(message = "Nome do contato é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do contato deve ter entre 2 e 100 caracteres")
    private String contactName;

    @NotBlank(message = "WhatsApp é obrigatório")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Formato de WhatsApp inválido")
    private String whatsapp;

    @Size(max = 200, message = "Website deve ter no máximo 200 caracteres")
    private String website;

    @Email(message = "Email deve ter formato válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;

    @NotNull(message = "Valor mínimo do pedido é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor mínimo deve ser maior que zero")
    private BigDecimal minimumOrderValue;

    @NotNull(message = "Prazo de entrega é obrigatório")
    @Min(value = 1, message = "Prazo de entrega deve ser de pelo menos 1 dia")
    @Max(value = 365, message = "Prazo de entrega não pode exceder 365 dias")
    private Integer deliveryTimeDays;

    @NotNull(message = "Porcentagem da taxa administrativa é obrigatória")
    @DecimalMin(value = "0.00", message = "Taxa administrativa não pode ser negativa")
    @DecimalMax(value = "100.00", message = "Taxa administrativa não pode exceder 100%")
    private BigDecimal adminFeePercentage;

    private List<String> categories;

    @Min(value = 1, message = "Rating deve ser entre 1 e 5")
    @Max(value = 5, message = "Rating deve ser entre 1 e 5")
    private Integer performanceRating = 5;

    @Size(max = 1000, message = "Notas devem ter no máximo 1000 caracteres")
    private String notes;
}
