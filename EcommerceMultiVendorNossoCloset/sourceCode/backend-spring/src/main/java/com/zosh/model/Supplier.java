package com.zosh.model;

import com.zosh.domain.SupplierStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Supplier - Fornecedores do sistema Nosso Closet
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nome do fornecedor é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nome do contato é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do contato deve ter entre 2 e 100 caracteres")
    private String contactName;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "WhatsApp é obrigatório")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Formato de WhatsApp inválido")
    private String whatsapp;

    @Column(length = 200)
    @Size(max = 200, message = "Website deve ter no máximo 200 caracteres")
    private String website;

    @Column(length = 100)
    @Email(message = "Email deve ter formato válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Valor mínimo do pedido é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor mínimo deve ser maior que zero")
    private BigDecimal minimumOrderValue;

    @Column(nullable = false)
    @NotNull(message = "Prazo de entrega é obrigatório")
    @Min(value = 1, message = "Prazo de entrega deve ser de pelo menos 1 dia")
    @Max(value = 365, message = "Prazo de entrega não pode exceder 365 dias")
    private Integer deliveryTimeDays;

    @Column(nullable = false, precision = 5, scale = 2)
    @NotNull(message = "Porcentagem da taxa administrativa é obrigatória")
    @DecimalMin(value = "0.00", message = "Taxa administrativa não pode ser negativa")
    @DecimalMax(value = "100.00", message = "Taxa administrativa não pode exceder 100%")
    private BigDecimal adminFeePercentage;

    @ElementCollection
    @CollectionTable(name = "supplier_categories", joinColumns = @JoinColumn(name = "supplier_id"))
    @Column(name = "category", length = 50)
    private List<String> categories = new ArrayList<>();

    @Column(nullable = false)
    @NotNull(message = "Rating de performance é obrigatório")
    @Min(value = 1, message = "Rating deve ser entre 1 e 5")
    @Max(value = 5, message = "Rating deve ser entre 1 e 5")
    private Integer performanceRating = 5;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupplierStatus status = SupplierStatus.ACTIVE;

    @Column(length = 1000)
    @Size(max = 1000, message = "Notas devem ter no máximo 1000 caracteres")
    private String notes;

    // Relacionamentos
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Catalog> catalogs = new ArrayList<>();

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CollectiveOrder> collectiveOrders = new ArrayList<>();

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomOrder> customOrders = new ArrayList<>();

    // Campos de auditoria
    @Column(length = 50)
    private String createdBy;

    @Column(length = 50)
    private String updatedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Métodos de conveniência
    public void addCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            this.categories.add(category.trim());
        }
    }

    public void removeCategory(String category) {
        this.categories.remove(category);
    }

    public boolean isActive() {
        return this.status == SupplierStatus.ACTIVE;
    }

    public void activate() {
        this.status = SupplierStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = SupplierStatus.INACTIVE;
    }

    public void suspend() {
        this.status = SupplierStatus.SUSPENDED;
    }

    /**
     * Calcula o valor com taxa administrativa
     */
    public BigDecimal calculateValueWithFee(BigDecimal baseValue) {
        if (baseValue == null || adminFeePercentage == null) {
            return baseValue;
        }
        
        BigDecimal feeMultiplier = adminFeePercentage.divide(BigDecimal.valueOf(100)).add(BigDecimal.ONE);
        return baseValue.multiply(feeMultiplier);
    }

    /**
     * Verifica se o valor atinge o mínimo do fornecedor
     */
    public boolean meetsMinimumOrder(BigDecimal orderValue) {
        if (orderValue == null || minimumOrderValue == null) {
            return false;
        }
        return orderValue.compareTo(minimumOrderValue) >= 0;
    }
}
