package com.zosh.repository;

import com.zosh.domain.CustomOrderStatus;
import com.zosh.domain.UrgencyLevel;
import com.zosh.model.CustomOrder;
import com.zosh.model.Supplier;
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
 * Repositório para entidade CustomOrder
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Repository
public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {

    /**
     * Busca pedidos por cliente
     */
    List<CustomOrder> findByClientOrderByCreatedAtDesc(User client);

    /**
     * Busca pedidos por cliente paginados
     */
    Page<CustomOrder> findByClient(User client, Pageable pageable);

    /**
     * Busca pedidos por status
     */
    List<CustomOrder> findByStatusOrderByCreatedAtAsc(CustomOrderStatus status);

    /**
     * Busca pedidos por status paginados
     */
    Page<CustomOrder> findByStatus(CustomOrderStatus status, Pageable pageable);

    /**
     * Busca pedidos por fornecedor
     */
    List<CustomOrder> findBySupplierOrderByCreatedAtDesc(Supplier supplier);

    /**
     * Busca pedidos por urgência
     */
    List<CustomOrder> findByUrgencyOrderByCreatedAtAsc(UrgencyLevel urgency);

    /**
     * Busca pedidos urgentes (HIGH ou URGENT)
     */
    List<CustomOrder> findByUrgencyInOrderByCreatedAtAsc(List<UrgencyLevel> urgencyLevels);

    /**
     * Busca pedidos pendentes de análise
     */
    @Query("SELECT co FROM CustomOrder co WHERE co.status = 'PENDING_ANALYSIS' ORDER BY co.urgency DESC, co.createdAt ASC")
    List<CustomOrder> findPendingAnalysis();

    /**
     * Busca pedidos precificados aguardando confirmação do cliente
     */
    List<CustomOrder> findByStatusAndAnalyzedAtIsNotNullOrderByAnalyzedAtDesc(CustomOrderStatus status);

    /**
     * Busca pedidos confirmados aguardando adição ao pedido coletivo
     */
    @Query("SELECT co FROM CustomOrder co WHERE co.status = 'CONFIRMED' AND co.collectiveOrder IS NULL ORDER BY co.confirmedAt ASC")
    List<CustomOrder> findConfirmedAwaitingCollectiveOrder();

    /**
     * Busca pedidos por cliente e status
     */
    List<CustomOrder> findByClientAndStatusOrderByCreatedAtDesc(User client, CustomOrderStatus status);

    /**
     * Busca pedidos por fornecedor e status
     */
    List<CustomOrder> findBySupplierAndStatusOrderByCreatedAtDesc(Supplier supplier, CustomOrderStatus status);

    /**
     * Busca pedidos por categoria
     */
    List<CustomOrder> findByCategoryOrderByCreatedAtDesc(String category);

    /**
     * Busca pedidos criados em um período
     */
    List<CustomOrder> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Busca pedidos com filtros avançados
     */
    @Query("SELECT co FROM CustomOrder co WHERE " +
           "(:status IS NULL OR co.status = :status) AND " +
           "(:supplierId IS NULL OR co.supplier.id = :supplierId) AND " +
           "(:urgency IS NULL OR co.urgency = :urgency) AND " +
           "(:category IS NULL OR LOWER(co.category) = LOWER(:category)) AND " +
           "(:minValue IS NULL OR co.finalPrice >= :minValue) AND " +
           "(:maxValue IS NULL OR co.finalPrice <= :maxValue) AND " +
           "(:startDate IS NULL OR co.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR co.createdAt <= :endDate) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(co.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(co.client.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(co.referenceCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CustomOrder> findWithFilters(@Param("status") CustomOrderStatus status,
                                     @Param("supplierId") Long supplierId,
                                     @Param("urgency") UrgencyLevel urgency,
                                     @Param("category") String category,
                                     @Param("minValue") BigDecimal minValue,
                                     @Param("maxValue") BigDecimal maxValue,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("searchTerm") String searchTerm,
                                     Pageable pageable);

    /**
     * Conta pedidos por status
     */
    long countByStatus(CustomOrderStatus status);

    /**
     * Conta pedidos por cliente
     */
    long countByClient(User client);

    /**
     * Conta pedidos por fornecedor
     */
    long countBySupplier(Supplier supplier);

    /**
     * Conta pedidos urgentes
     */
    @Query("SELECT COUNT(co) FROM CustomOrder co WHERE co.urgency IN ('HIGH', 'URGENT') AND co.status NOT IN ('DELIVERED', 'CANCELLED')")
    long countUrgentPendingOrders();

    /**
     * Busca pedidos por referência
     */
    List<CustomOrder> findByReferenceCodeContainingIgnoreCase(String referenceCode);

    /**
     * Busca pedidos antigos sem análise
     */
    @Query("SELECT co FROM CustomOrder co WHERE co.status = 'PENDING_ANALYSIS' AND co.createdAt < :cutoffDate ORDER BY co.createdAt ASC")
    List<CustomOrder> findOldPendingAnalysis(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Busca pedidos precificados há muito tempo sem confirmação
     */
    @Query("SELECT co FROM CustomOrder co WHERE co.status = 'PRICED' AND co.analyzedAt < :cutoffDate ORDER BY co.analyzedAt ASC")
    List<CustomOrder> findOldPricedOrders(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Busca estatísticas de pedidos por período
     */
    @Query("SELECT co.status, COUNT(co), AVG(co.finalPrice), SUM(co.finalPrice) " +
           "FROM CustomOrder co " +
           "WHERE co.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY co.status")
    List<Object[]> getOrderStatisticsByPeriod(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * Busca pedidos mais populares por categoria
     */
    @Query("SELECT co.category, COUNT(co) as orderCount " +
           "FROM CustomOrder co " +
           "WHERE co.status NOT IN ('CANCELLED') " +
           "GROUP BY co.category " +
           "ORDER BY orderCount DESC")
    List<Object[]> getMostPopularCategories();

    /**
     * Busca tempo médio de análise de pedidos
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', HOUR, co.createdAt, co.analyzedAt)) " +
           "FROM CustomOrder co " +
           "WHERE co.analyzedAt IS NOT NULL AND co.analyzedAt > co.createdAt")
    Double getAverageAnalysisTimeHours();

    /**
     * Busca pedidos que precisam de follow-up
     */
    @Query("SELECT co FROM CustomOrder co WHERE " +
           "(co.status = 'PENDING_ANALYSIS' AND co.createdAt < :urgentCutoff) OR " +
           "(co.status = 'PRICED' AND co.analyzedAt < :pricedCutoff) " +
           "ORDER BY co.urgency DESC, co.createdAt ASC")
    List<CustomOrder> findOrdersNeedingFollowUp(@Param("urgentCutoff") LocalDateTime urgentCutoff,
                                               @Param("pricedCutoff") LocalDateTime pricedCutoff);

    /**
     * Busca últimos pedidos de um cliente
     */
    List<CustomOrder> findTop10ByClientOrderByCreatedAtDesc(User client);

    /**
     * Verifica se cliente tem pedidos em andamento
     */
    boolean existsByClientAndStatusIn(User client, List<CustomOrderStatus> statuses);
}
