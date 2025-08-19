package com.zosh.model;

import com.zosh.domain.CustomOrderStatus;
import com.zosh.domain.UrgencyLevel;
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
 * Entidade CustomOrder - Pedidos personalizados dos clientes
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Entity
@Table(name = "custom_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Cliente é obrigatório")
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier; // Pode ser null inicialmente

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collective_order_id")
    private CollectiveOrder collectiveOrder; // Definido quando adicionado ao pedido coletivo

    @Column(nullable = false, length = 500)
    @NotBlank(message = "URL da imagem do produto é obrigatória")
    @Size(max = 500, message = "URL da imagem deve ter no máximo 500 caracteres")
    private String productImageUrl; // URL da imagem no S3

    @Column(length = 50)
    @Size(max = 50, message = "Código de referência deve ter no máximo 50 caracteres")
    private String referenceCode; // Código de referência se disponível

    @Column(nullable = false, length = 1000)
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 1000, message = "Descrição deve ter entre 10 e 1000 caracteres")
    private String description;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Cor preferencial é obrigatória")
    @Size(max = 50, message = "Cor preferencial deve ter no máximo 50 caracteres")
    private String preferredColor;

    @ElementCollection
    @CollectionTable(name = "custom_order_alternative_colors", joinColumns = @JoinColumn(name = "custom_order_id"))
    @Column(name = "color", length = 50)
    private List<String> alternativeColors = new ArrayList<>();

    @Column(nullable = false, length = 30)
    @NotBlank(message = "Tamanho é obrigatório")
    @Size(max = 30, message = "Tamanho deve ter no máximo 30 caracteres")
    private String size;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Categoria é obrigatória")
    @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
    private String category;

    @Column(length = 500)
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observations;

    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Preço estimado não pode ser negativo")
    private BigDecimal estimatedPrice;

    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Preço final não pode ser negativo")
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomOrderStatus status = CustomOrderStatus.PENDING_ANALYSIS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UrgencyLevel urgency = UrgencyLevel.NORMAL;

    @Column(nullable = false)
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Max(value = 10, message = "Quantidade máxima é 10 por pedido")
    private Integer quantity = 1;

    @Column(length = 500)
    @Size(max = 500, message = "Notas administrativas devem ter no máximo 500 caracteres")
    private String adminNotes;

    @Column(length = 50)
    private String analyzedBy; // Admin que analisou o pedido

    @Column(length = 200)
    @Size(max = 200, message = "Motivo de cancelamento deve ter no máximo 200 caracteres")
    private String cancellationReason;

    // Timestamps específicos
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime analyzedAt;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column
    private LocalDateTime deliveredAt;

    // Campos de auditoria
    @Column(length = 50)
    private String createdBy;

    @Column(length = 50)
    private String updatedBy;

    // Métodos de conveniência
    public void addAlternativeColor(String color) {
        if (color != null && !color.trim().isEmpty()) {
            this.alternativeColors.add(color.trim());
        }
    }

    public void removeAlternativeColor(String color) {
        this.alternativeColors.remove(color);
    }

    public boolean isPending() {
        return this.status == CustomOrderStatus.PENDING_ANALYSIS;
    }

    public boolean isPriced() {
        return this.status == CustomOrderStatus.PRICED;
    }

    public boolean isConfirmed() {
        return this.status == CustomOrderStatus.CONFIRMED;
    }

    public boolean isCancelled() {
        return this.status == CustomOrderStatus.CANCELLED;
    }

    public boolean isDelivered() {
        return this.status == CustomOrderStatus.DELIVERED;
    }

    public void analyze(String adminId, BigDecimal price, Supplier supplier) {
        this.analyzedBy = adminId;
        this.finalPrice = price;
        this.supplier = supplier;
        this.status = CustomOrderStatus.PRICED;
        this.analyzedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != CustomOrderStatus.PRICED) {
            throw new IllegalStateException("Só é possível confirmar pedidos precificados");
        }
        this.status = CustomOrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        this.status = CustomOrderStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void addToCollectiveOrder(CollectiveOrder collectiveOrder) {
        if (this.status != CustomOrderStatus.CONFIRMED) {
            throw new IllegalStateException("Só é possível adicionar pedidos confirmados ao pedido coletivo");
        }
        this.collectiveOrder = collectiveOrder;
        this.status = CustomOrderStatus.IN_COLLECTIVE;
    }

    public void markAsDelivered() {
        this.status = CustomOrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Calcula o valor total do pedido (preço final * quantidade)
     */
    public BigDecimal getTotalValue() {
        if (finalPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return finalPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Verifica se o pedido é urgente
     */
    public boolean isUrgent() {
        return this.urgency == UrgencyLevel.URGENT || this.urgency == UrgencyLevel.HIGH;
    }

    /**
     * Calcula há quantos dias o pedido foi criado
     */
    public long getDaysOld() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }
}
