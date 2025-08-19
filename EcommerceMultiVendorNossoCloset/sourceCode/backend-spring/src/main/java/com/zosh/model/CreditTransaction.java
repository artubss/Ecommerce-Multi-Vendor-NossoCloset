package com.zosh.model;

import com.zosh.domain.CreditStatus;
import com.zosh.domain.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade CreditTransaction - Movimentações de crédito dos clientes
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Entity
@Table(name = "credit_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Cliente é obrigatório")
    private User client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Tipo de transação é obrigatório")
    private TransactionType transactionType;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 5, max = 500, message = "Descrição deve ter entre 5 e 500 caracteres")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_order_id")
    private CollectiveOrder referenceOrder; // Pedido coletivo relacionado (se aplicável)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_custom_order_id")
    private CustomOrder referenceCustomOrder; // Pedido personalizado relacionado (se aplicável)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime usedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditStatus status = CreditStatus.ACTIVE;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Saldo após transação é obrigatório")
    @DecimalMin(value = "0.00", message = "Saldo não pode ser negativo")
    private BigDecimal balanceAfterTransaction;

    @Column(length = 100)
    @Size(max = 100, message = "Código de referência deve ter no máximo 100 caracteres")
    private String referenceCode; // Para rastreamento externo

    @Column(length = 200)
    @Size(max = 200, message = "Observações devem ter no máximo 200 caracteres")
    private String observations;

    @Column(precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Percentual de bônus não pode ser negativo")
    @DecimalMax(value = "100.00", message = "Percentual de bônus não pode exceder 100%")
    private BigDecimal bonusPercentage; // Para transações de bônus

    // Relacionamentos para transferências
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_from_client_id")
    private User transferFromClient; // Cliente que transferiu (para TRANSFER)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_to_client_id")
    private User transferToClient; // Cliente que recebeu (para TRANSFER)

    // Campos de auditoria
    @Column(length = 50)
    private String createdBy;

    @Column(length = 50)
    private String processedBy; // Admin que processou a transação

    // Métodos de conveniência
    public boolean isActive() {
        return this.status == CreditStatus.ACTIVE;
    }

    public boolean isExpired() {
        return this.status == CreditStatus.EXPIRED || 
               (expiresAt != null && LocalDateTime.now().isAfter(expiresAt));
    }

    public boolean isUsed() {
        return this.status == CreditStatus.USED;
    }

    public boolean isRefund() {
        return this.transactionType == TransactionType.REFUND;
    }

    public boolean isBonus() {
        return this.transactionType == TransactionType.CREDIT_BONUS ||
               this.transactionType == TransactionType.REFERRAL_BONUS ||
               this.transactionType == TransactionType.LOYALTY_BONUS ||
               this.transactionType == TransactionType.PROMOTIONAL_CREDIT;
    }

    public boolean isTransfer() {
        return this.transactionType == TransactionType.TRANSFER;
    }

    public boolean isDebit() {
        return this.transactionType == TransactionType.EXPIRATION_DEBIT ||
               this.transactionType == TransactionType.USAGE_DEBIT;
    }

    public void use() {
        if (!isActive()) {
            throw new IllegalStateException("Só é possível usar créditos ativos");
        }
        
        if (isExpired()) {
            throw new IllegalStateException("Crédito está expirado");
        }
        
        this.status = CreditStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void expire() {
        if (this.status == CreditStatus.USED) {
            throw new IllegalStateException("Crédito já foi usado");
        }
        
        this.status = CreditStatus.EXPIRED;
    }

    public void block() {
        this.status = CreditStatus.BLOCKED;
    }

    public void activate() {
        if (isExpired()) {
            throw new IllegalStateException("Não é possível ativar um crédito expirado");
        }
        
        this.status = CreditStatus.ACTIVE;
    }

    /**
     * Verifica se o crédito está próximo do vencimento (menos de 7 dias)
     */
    public boolean isNearExpiration() {
        if (expiresAt == null) {
            return false;
        }
        
        LocalDateTime sevenDaysFromNow = LocalDateTime.now().plusDays(7);
        return expiresAt.isBefore(sevenDaysFromNow) && isActive();
    }

    /**
     * Calcula quantos dias restam até o vencimento
     */
    public long getDaysUntilExpiration() {
        if (expiresAt == null) {
            return Long.MAX_VALUE;
        }
        
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt);
        return Math.max(0, days);
    }

    /**
     * Cria uma transação de reembolso
     */
    public static CreditTransaction createRefund(User client, BigDecimal amount, String description, 
                                               CollectiveOrder referenceOrder, BigDecimal newBalance) {
        CreditTransaction transaction = new CreditTransaction();
        transaction.setClient(client);
        transaction.setTransactionType(TransactionType.REFUND);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setReferenceOrder(referenceOrder);
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setStatus(CreditStatus.ACTIVE);
        
        // Créditos de reembolso expiram em 1 ano
        transaction.setExpiresAt(LocalDateTime.now().plusYears(1));
        
        return transaction;
    }

    /**
     * Cria uma transação de bônus
     */
    public static CreditTransaction createBonus(User client, TransactionType bonusType, BigDecimal amount, 
                                              String description, BigDecimal bonusPercentage, BigDecimal newBalance) {
        if (!bonusType.name().contains("BONUS")) {
            throw new IllegalArgumentException("Tipo de transação deve ser um bônus");
        }
        
        CreditTransaction transaction = new CreditTransaction();
        transaction.setClient(client);
        transaction.setTransactionType(bonusType);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBonusPercentage(bonusPercentage);
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setStatus(CreditStatus.ACTIVE);
        
        // Bônus expiram em 6 meses
        transaction.setExpiresAt(LocalDateTime.now().plusMonths(6));
        
        return transaction;
    }

    /**
     * Cria uma transação de transferência
     */
    public static CreditTransaction createTransfer(User fromClient, User toClient, BigDecimal amount, 
                                                 String description, BigDecimal newBalance) {
        CreditTransaction transaction = new CreditTransaction();
        transaction.setClient(toClient); // Cliente que recebe
        transaction.setTransferFromClient(fromClient);
        transaction.setTransferToClient(toClient);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setStatus(CreditStatus.ACTIVE);
        
        // Transferências não expiram automaticamente
        
        return transaction;
    }
}
