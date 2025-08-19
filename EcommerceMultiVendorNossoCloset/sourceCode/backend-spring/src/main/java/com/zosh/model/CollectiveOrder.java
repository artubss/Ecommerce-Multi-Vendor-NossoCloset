package com.zosh.model;

import com.zosh.domain.CollectiveOrderStatus;
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
 * Entidade CollectiveOrder - Pedidos coletivos por fornecedor
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Entity
@Table(name = "collective_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectiveOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @NotNull(message = "Fornecedor é obrigatório")
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CollectiveOrderStatus status = CollectiveOrderStatus.OPEN;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Valor mínimo é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor mínimo deve ser maior que zero")
    private BigDecimal minimumValue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(nullable = false)
    @NotNull(message = "Prazo de pagamento é obrigatório")
    @Future(message = "Prazo de pagamento deve ser no futuro")
    private LocalDateTime paymentDeadline;

    @Column
    private LocalDateTime supplierPaymentDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal anticipatedAmount = BigDecimal.ZERO; // Valor que a empresa pagou

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalReceived = BigDecimal.ZERO; // Valor recebido dos clientes

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal profitMargin = BigDecimal.ZERO;

    @Column(length = 500)
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observations;

    @Column(length = 100)
    @Size(max = 100, message = "Código de rastreamento deve ter no máximo 100 caracteres")
    private String trackingCode;

    @Column
    private LocalDateTime expectedDeliveryDate;

    @Column
    private LocalDateTime actualDeliveryDate;

    // Relacionamentos
    @OneToMany(mappedBy = "collectiveOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomOrder> customOrders = new ArrayList<>();

    // Timestamps específicos
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime closedAt;

    @Column
    private LocalDateTime minimumReachedAt;

    @Column
    private LocalDateTime paymentWindowOpenedAt;

    @Column
    private LocalDateTime shippedAt;

    @Column
    private LocalDateTime deliveredAt;

    // Campos de auditoria
    @Column(length = 50)
    private String createdBy;

    @Column(length = 50)
    private String updatedBy;

    @Column(length = 50)
    private String closedBy;

    // Métodos de conveniência
    public boolean isOpen() {
        return this.status == CollectiveOrderStatus.OPEN;
    }

    public boolean isMinimumReached() {
        return currentValue.compareTo(minimumValue) >= 0;
    }

    public boolean isClosed() {
        return this.status == CollectiveOrderStatus.CLOSED || 
               this.status == CollectiveOrderStatus.CANCELLED;
    }

    public void addCustomOrder(CustomOrder customOrder) {
        if (!isOpen()) {
            throw new IllegalStateException("Não é possível adicionar pedidos a um pedido coletivo fechado");
        }

        customOrder.addToCollectiveOrder(this);
        this.customOrders.add(customOrder);
        
        // Recalcular valor atual
        recalculateCurrentValue();
        
        // Verificar se atingiu o mínimo
        if (isMinimumReached() && this.status == CollectiveOrderStatus.OPEN) {
            reachMinimum();
        }
    }

    public void removeCustomOrder(CustomOrder customOrder) {
        this.customOrders.remove(customOrder);
        customOrder.setCollectiveOrder(null);
        recalculateCurrentValue();
    }

    public void reachMinimum() {
        if (!isMinimumReached()) {
            throw new IllegalStateException("Valor mínimo ainda não foi atingido");
        }
        
        this.status = CollectiveOrderStatus.MINIMUM_REACHED;
        this.minimumReachedAt = LocalDateTime.now();
    }

    public void openPaymentWindow() {
        if (this.status != CollectiveOrderStatus.MINIMUM_REACHED) {
            throw new IllegalStateException("Só é possível abrir janela de pagamento após atingir o mínimo");
        }
        
        this.status = CollectiveOrderStatus.PAYMENT_WINDOW;
        this.paymentWindowOpenedAt = LocalDateTime.now();
    }

    public void paySupplier(BigDecimal amount) {
        this.anticipatedAmount = amount;
        this.supplierPaymentDate = LocalDateTime.now();
        this.status = CollectiveOrderStatus.SUPPLIER_PAID;
        
        // Atualizar status dos pedidos personalizados
        for (CustomOrder customOrder : customOrders) {
            customOrder.setStatus(CustomOrderStatus.SUPPLIER_PAID);
        }
    }

    public void markAsShipped(String trackingCode) {
        this.trackingCode = trackingCode;
        this.shippedAt = LocalDateTime.now();
        this.status = CollectiveOrderStatus.IN_TRANSIT;
        
        // Atualizar status dos pedidos personalizados
        for (CustomOrder customOrder : customOrders) {
            customOrder.setStatus(CustomOrderStatus.IN_TRANSIT);
        }
    }

    public void markAsReceived() {
        this.status = CollectiveOrderStatus.RECEIVED;
        this.actualDeliveryDate = LocalDateTime.now();
        
        // Atualizar status dos pedidos personalizados
        for (CustomOrder customOrder : customOrders) {
            customOrder.setStatus(CustomOrderStatus.RECEIVED);
        }
    }

    public void markAsDelivered() {
        this.status = CollectiveOrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        
        // Atualizar status dos pedidos personalizados
        for (CustomOrder customOrder : customOrders) {
            customOrder.markAsDelivered();
        }
    }

    public void close(String adminId) {
        this.status = CollectiveOrderStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.closedBy = adminId;
        
        // Calcular margem de lucro final
        calculateFinalProfitMargin();
    }

    public void cancel(String reason) {
        this.status = CollectiveOrderStatus.CANCELLED;
        this.closedAt = LocalDateTime.now();
        this.observations = (observations != null ? observations + " | " : "") + "CANCELADO: " + reason;
        
        // Remover pedidos personalizados do coletivo
        for (CustomOrder customOrder : new ArrayList<>(customOrders)) {
            removeCustomOrder(customOrder);
            customOrder.setStatus(CustomOrderStatus.CONFIRMED); // Volta para confirmado
        }
    }

    private void recalculateCurrentValue() {
        this.currentValue = customOrders.stream()
                .map(CustomOrder::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void calculateFinalProfitMargin() {
        if (totalReceived.compareTo(BigDecimal.ZERO) > 0 && anticipatedAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.profitMargin = totalReceived.subtract(anticipatedAmount);
        }
    }

    /**
     * Calcula o percentual de completude do pedido coletivo
     */
    public double getCompletionPercentage() {
        if (minimumValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        double current = currentValue.doubleValue();
        double minimum = minimumValue.doubleValue();
        
        return Math.min((current / minimum) * 100.0, 100.0);
    }

    /**
     * Retorna quantos clientes estão participando do pedido coletivo
     */
    public int getParticipantCount() {
        return (int) customOrders.stream()
                .map(CustomOrder::getClient)
                .distinct()
                .count();
    }

    /**
     * Calcula quanto ainda falta para atingir o mínimo
     */
    public BigDecimal getRemainingAmount() {
        BigDecimal remaining = minimumValue.subtract(currentValue);
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
    }

    /**
     * Verifica se o prazo de pagamento está próximo (menos de 24h)
     */
    public boolean isPaymentDeadlineNear() {
        if (paymentDeadline == null) {
            return false;
        }
        
        LocalDateTime twentyFourHoursFromNow = LocalDateTime.now().plusHours(24);
        return paymentDeadline.isBefore(twentyFourHoursFromNow);
    }

    /**
     * Verifica se o prazo de pagamento já passou
     */
    public boolean isPaymentOverdue() {
        if (paymentDeadline == null) {
            return false;
        }
        
        return LocalDateTime.now().isAfter(paymentDeadline);
    }
}
