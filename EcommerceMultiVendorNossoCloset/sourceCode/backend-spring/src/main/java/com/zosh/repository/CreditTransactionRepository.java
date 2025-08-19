package com.zosh.repository;

import com.zosh.domain.CreditStatus;
import com.zosh.domain.TransactionType;
import com.zosh.model.CreditTransaction;
import com.zosh.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para entidade CreditTransaction
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

    /**
     * Busca transações por cliente
     */
    List<CreditTransaction> findByClientOrderByCreatedAtDesc(User client);

    /**
     * Busca transações por cliente paginadas
     */
    Page<CreditTransaction> findByClient(User client, Pageable pageable);

    /**
     * Busca transações ativas do cliente
     */
    List<CreditTransaction> findByClientAndStatusOrderByCreatedAtDesc(User client, CreditStatus status);

    /**
     * Busca transações por tipo
     */
    List<CreditTransaction> findByTransactionTypeOrderByCreatedAtDesc(TransactionType transactionType);

    /**
     * Busca transações por status
     */
    List<CreditTransaction> findByStatusOrderByCreatedAtDesc(CreditStatus status);

    /**
     * Busca créditos ativos do cliente
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.client = :client AND ct.status = 'ACTIVE' AND " +
           "(ct.expiresAt IS NULL OR ct.expiresAt > :now) " +
           "ORDER BY ct.expiresAt ASC NULLS LAST, ct.createdAt ASC")
    List<CreditTransaction> findActiveCredits(@Param("client") User client, @Param("now") LocalDateTime now);

    /**
     * Busca créditos próximos ao vencimento
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.status = 'ACTIVE' AND ct.expiresAt IS NOT NULL AND " +
           "ct.expiresAt BETWEEN :now AND :cutoff ORDER BY ct.expiresAt ASC")
    List<CreditTransaction> findCreditsNearExpiration(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);

    /**
     * Busca créditos expirados
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.status = 'ACTIVE' AND ct.expiresAt IS NOT NULL AND " +
           "ct.expiresAt < :now ORDER BY ct.expiresAt ASC")
    List<CreditTransaction> findExpiredCredits(@Param("now") LocalDateTime now);

    /**
     * Calcula saldo total de créditos ativos do cliente
     */
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct WHERE ct.client = :client AND ct.status = 'ACTIVE' AND " +
           "(ct.expiresAt IS NULL OR ct.expiresAt > :now)")
    BigDecimal calculateActiveCreditBalance(@Param("client") User client, @Param("now") LocalDateTime now);

    /**
     * Busca transações criadas em um período
     */
    List<CreditTransaction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Busca transações com filtros
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE " +
           "(:clientId IS NULL OR ct.client.id = :clientId) AND " +
           "(:transactionType IS NULL OR ct.transactionType = :transactionType) AND " +
           "(:status IS NULL OR ct.status = :status) AND " +
           "(:minAmount IS NULL OR ct.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR ct.amount <= :maxAmount) AND " +
           "(:startDate IS NULL OR ct.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR ct.createdAt <= :endDate) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(ct.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(ct.referenceCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CreditTransaction> findWithFilters(@Param("clientId") Long clientId,
                                          @Param("transactionType") TransactionType transactionType,
                                          @Param("status") CreditStatus status,
                                          @Param("minAmount") BigDecimal minAmount,
                                          @Param("maxAmount") BigDecimal maxAmount,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("searchTerm") String searchTerm,
                                          Pageable pageable);

    /**
     * Conta transações por tipo
     */
    long countByTransactionType(TransactionType transactionType);

    /**
     * Conta transações por status
     */
    long countByStatus(CreditStatus status);

    /**
     * Conta transações do cliente
     */
    long countByClient(User client);

    /**
     * Busca valor total de créditos emitidos por período
     */
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct WHERE " +
           "ct.transactionType IN ('REFUND', 'CREDIT_BONUS', 'REFERRAL_BONUS', 'LOYALTY_BONUS', 'PROMOTIONAL_CREDIT') AND " +
           "ct.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCreditsIssuedByPeriod(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Busca valor total de créditos utilizados por período
     */
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct WHERE " +
           "ct.transactionType = 'USAGE_DEBIT' AND " +
           "ct.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCreditsUsedByPeriod(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Busca estatísticas de créditos por período
     */
    @Query("SELECT ct.transactionType, COUNT(ct), SUM(ct.amount), AVG(ct.amount) " +
           "FROM CreditTransaction ct " +
           "WHERE ct.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY ct.transactionType " +
           "ORDER BY SUM(ct.amount) DESC")
    List<Object[]> getCreditStatisticsByPeriod(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Busca transações por código de referência
     */
    List<CreditTransaction> findByReferenceCodeContainingIgnoreCase(String referenceCode);

    /**
     * Busca reembolsos por pedido coletivo
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.referenceOrder.id = :orderId AND ct.transactionType = 'REFUND' ORDER BY ct.createdAt DESC")
    List<CreditTransaction> findRefundsByCollectiveOrder(@Param("orderId") Long orderId);

    /**
     * Busca transferências de um cliente
     */
    List<CreditTransaction> findByTransferFromClientOrderByCreatedAtDesc(User fromClient);

    /**
     * Busca transferências para um cliente
     */
    List<CreditTransaction> findByTransferToClientOrderByCreatedAtDesc(User toClient);

    /**
     * Busca clientes com mais créditos ativos
     */
    @Query("SELECT ct.client, COALESCE(SUM(ct.amount), 0) as totalCredits " +
           "FROM CreditTransaction ct " +
           "WHERE ct.status = 'ACTIVE' AND (ct.expiresAt IS NULL OR ct.expiresAt > :now) " +
           "GROUP BY ct.client " +
           "HAVING totalCredits > 0 " +
           "ORDER BY totalCredits DESC")
    List<Object[]> getClientsWithMostActiveCredits(@Param("now") LocalDateTime now);

    /**
     * Busca valor médio de transações por tipo
     */
    @Query("SELECT AVG(ct.amount) FROM CreditTransaction ct WHERE ct.transactionType = :transactionType")
    BigDecimal getAverageAmountByType(@Param("transactionType") TransactionType transactionType);

    /**
     * Busca últimas transações do cliente
     */
    List<CreditTransaction> findTop10ByClientOrderByCreatedAtDesc(User client);

    /**
     * Verifica se cliente tem créditos suficientes
     */
    @Query("SELECT CASE WHEN COALESCE(SUM(ct.amount), 0) >= :requiredAmount THEN true ELSE false END " +
           "FROM CreditTransaction ct WHERE ct.client = :client AND ct.status = 'ACTIVE' AND " +
           "(ct.expiresAt IS NULL OR ct.expiresAt > :now)")
    boolean clientHasSufficientCredits(@Param("client") User client, 
                                     @Param("requiredAmount") BigDecimal requiredAmount, 
                                     @Param("now") LocalDateTime now);

    /**
     * Busca transações que precisam de follow-up
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE " +
           "(ct.status = 'PENDING' AND ct.createdAt < :pendingCutoff) OR " +
           "(ct.status = 'ACTIVE' AND ct.expiresAt IS NOT NULL AND ct.expiresAt < :now) " +
           "ORDER BY " +
           "CASE " +
           "  WHEN ct.status = 'ACTIVE' AND ct.expiresAt < :now THEN 1 " +
           "  WHEN ct.status = 'PENDING' THEN 2 " +
           "  ELSE 3 " +
           "END, ct.createdAt ASC")
    List<CreditTransaction> findTransactionsNeedingFollowUp(@Param("pendingCutoff") LocalDateTime pendingCutoff,
                                                          @Param("now") LocalDateTime now);

    /**
     * Busca valor total de bônus distribuídos
     */
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct WHERE " +
           "ct.transactionType IN ('CREDIT_BONUS', 'REFERRAL_BONUS', 'LOYALTY_BONUS') AND " +
           "ct.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalBonusDistributedByPeriod(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Busca taxa de utilização de créditos
     */
    @Query("SELECT " +
           "(SELECT COALESCE(SUM(ct1.amount), 0) FROM CreditTransaction ct1 WHERE ct1.transactionType = 'USAGE_DEBIT' AND ct1.createdAt BETWEEN :startDate AND :endDate) / " +
           "(SELECT COALESCE(SUM(ct2.amount), 1) FROM CreditTransaction ct2 WHERE ct2.transactionType IN ('REFUND', 'CREDIT_BONUS', 'REFERRAL_BONUS', 'LOYALTY_BONUS', 'PROMOTIONAL_CREDIT') AND ct2.createdAt BETWEEN :startDate AND :endDate) * 100")
    Double getCreditUtilizationRateByPeriod(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
}
