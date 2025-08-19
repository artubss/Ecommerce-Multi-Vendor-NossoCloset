package com.zosh.repository;

import com.zosh.domain.CollectiveOrderStatus;
import com.zosh.model.CollectiveOrder;
import com.zosh.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para entidade CollectiveOrder
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Repository
public interface CollectiveOrderRepository extends JpaRepository<CollectiveOrder, Long> {

    /**
     * Busca pedidos coletivos por fornecedor
     */
    List<CollectiveOrder> findBySupplierOrderByCreatedAtDesc(Supplier supplier);

    /**
     * Busca pedidos coletivos por status
     */
    List<CollectiveOrder> findByStatusOrderByCreatedAtDesc(CollectiveOrderStatus status);

    /**
     * Busca pedidos coletivos por status paginados
     */
    Page<CollectiveOrder> findByStatus(CollectiveOrderStatus status, Pageable pageable);

    /**
     * Busca pedido coletivo aberto para um fornecedor específico
     */
    Optional<CollectiveOrder> findBySupplierAndStatus(Supplier supplier, CollectiveOrderStatus status);

    /**
     * Busca pedidos coletivos abertos
     */
    List<CollectiveOrder> findByStatusOrderByMinimumReachedAtDesc(CollectiveOrderStatus status);

    /**
     * Busca pedidos coletivos que atingiram o mínimo
     */
    @Query("SELECT co FROM CollectiveOrder co WHERE co.currentValue >= co.minimumValue AND co.status = 'OPEN' ORDER BY co.minimumReachedAt ASC")
    List<CollectiveOrder> findOrdersReachedMinimum();

    /**
     * Busca pedidos coletivos próximos ao prazo de pagamento
     */
    @Query("SELECT co FROM CollectiveOrder co WHERE co.status = 'PAYMENT_WINDOW' AND co.paymentDeadline BETWEEN :now AND :cutoff ORDER BY co.paymentDeadline ASC")
    List<CollectiveOrder> findOrdersNearPaymentDeadline(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);

    /**
     * Busca pedidos coletivos com prazo de pagamento vencido
     */
    @Query("SELECT co FROM CollectiveOrder co WHERE co.status = 'PAYMENT_WINDOW' AND co.paymentDeadline < :now ORDER BY co.paymentDeadline ASC")
    List<CollectiveOrder> findOverduePaymentOrders(@Param("now") LocalDateTime now);

    /**
     * Busca pedidos coletivos em trânsito
     */
    List<CollectiveOrder> findByStatusAndTrackingCodeIsNotNullOrderByShippedAtDesc(CollectiveOrderStatus status);

    /**
     * Busca pedidos coletivos criados em um período
     */
    List<CollectiveOrder> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Busca pedidos coletivos com filtros
     */
    @Query("SELECT co FROM CollectiveOrder co WHERE " +
           "(:status IS NULL OR co.status = :status) AND " +
           "(:supplierId IS NULL OR co.supplier.id = :supplierId) AND " +
           "(:minValue IS NULL OR co.currentValue >= :minValue) AND " +
           "(:maxValue IS NULL OR co.currentValue <= :maxValue) AND " +
           "(:startDate IS NULL OR co.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR co.createdAt <= :endDate) AND " +
           "(:hasTrackingCode IS NULL OR " +
           " (:hasTrackingCode = true AND co.trackingCode IS NOT NULL) OR " +
           " (:hasTrackingCode = false AND co.trackingCode IS NULL))")
    Page<CollectiveOrder> findWithFilters(@Param("status") CollectiveOrderStatus status,
                                        @Param("supplierId") Long supplierId,
                                        @Param("minValue") BigDecimal minValue,
                                        @Param("maxValue") BigDecimal maxValue,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        @Param("hasTrackingCode") Boolean hasTrackingCode,
                                        Pageable pageable);

    /**
     * Conta pedidos coletivos por status
     */
    long countByStatus(CollectiveOrderStatus status);

    /**
     * Conta pedidos coletivos por fornecedor
     */
    long countBySupplier(Supplier supplier);

    /**
     * Busca valor total de pedidos coletivos por fornecedor
     */
    @Query("SELECT SUM(co.currentValue) FROM CollectiveOrder co WHERE co.supplier = :supplier AND co.status NOT IN ('CANCELLED')")
    BigDecimal getTotalValueBySupplier(@Param("supplier") Supplier supplier);

    /**
     * Busca valor total antecipado por fornecedor
     */
    @Query("SELECT SUM(co.anticipatedAmount) FROM CollectiveOrder co WHERE co.supplier = :supplier AND co.anticipatedAmount > 0")
    BigDecimal getTotalAnticipatedBySupplier(@Param("supplier") Supplier supplier);

    /**
     * Busca estatísticas de pedidos coletivos por período
     */
    @Query("SELECT co.status, COUNT(co), AVG(co.currentValue), SUM(co.currentValue), AVG(co.anticipatedAmount) " +
           "FROM CollectiveOrder co " +
           "WHERE co.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY co.status")
    List<Object[]> getOrderStatisticsByPeriod(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * Busca pedidos coletivos mais rentáveis
     */
    @Query("SELECT co FROM CollectiveOrder co WHERE co.status = 'CLOSED' AND co.profitMargin > 0 ORDER BY co.profitMargin DESC")
    List<CollectiveOrder> findMostProfitableOrders(Pageable pageable);

    /**
     * Busca tempo médio para atingir mínimo
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', HOUR, co.createdAt, co.minimumReachedAt)) " +
           "FROM CollectiveOrder co " +
           "WHERE co.minimumReachedAt IS NOT NULL")
    Double getAverageTimeToReachMinimumHours();

    /**
     * Busca tempo médio de entrega
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', DAY, co.supplierPaymentDate, co.actualDeliveryDate)) " +
           "FROM CollectiveOrder co " +
           "WHERE co.supplierPaymentDate IS NOT NULL AND co.actualDeliveryDate IS NOT NULL")
    Double getAverageDeliveryTimeDays();

    /**
     * Busca pedidos coletivos que precisam de ação administrativa
     */
    @Query("SELECT co FROM CollectiveOrder co WHERE " +
           "(co.status = 'OPEN' AND co.currentValue >= co.minimumValue) OR " +
           "(co.status = 'MINIMUM_REACHED' AND co.paymentWindowOpenedAt IS NULL) OR " +
           "(co.status = 'PAYMENT_WINDOW' AND co.paymentDeadline < :now) " +
           "ORDER BY " +
           "CASE " +
           "  WHEN co.status = 'PAYMENT_WINDOW' AND co.paymentDeadline < :now THEN 1 " +
           "  WHEN co.status = 'OPEN' AND co.currentValue >= co.minimumValue THEN 2 " +
           "  WHEN co.status = 'MINIMUM_REACHED' THEN 3 " +
           "  ELSE 4 " +
           "END, co.createdAt ASC")
    List<CollectiveOrder> findOrdersNeedingAdminAction(@Param("now") LocalDateTime now);

    /**
     * Busca pedidos coletivos prontos para pagamento ao fornecedor
     */
    @Query("SELECT co FROM CollectiveOrder co WHERE co.status = 'PAYMENT_WINDOW' AND co.supplierPaymentDate IS NULL ORDER BY co.paymentWindowOpenedAt ASC")
    List<CollectiveOrder> findOrdersReadyForSupplierPayment();

    /**
     * Busca fornecedores com mais pedidos coletivos
     */
    @Query("SELECT co.supplier, COUNT(co) as orderCount, SUM(co.currentValue) as totalValue " +
           "FROM CollectiveOrder co " +
           "WHERE co.status NOT IN ('CANCELLED') " +
           "GROUP BY co.supplier " +
           "ORDER BY orderCount DESC, totalValue DESC")
    List<Object[]> getTopSuppliersByOrderCount();

    /**
     * Busca pedidos coletivos por código de rastreamento
     */
    Optional<CollectiveOrder> findByTrackingCode(String trackingCode);

    /**
     * Verifica se existe pedido coletivo aberto para o fornecedor
     */
    boolean existsBySupplierAndStatusIn(Supplier supplier, List<CollectiveOrderStatus> statuses);

    /**
     * Busca últimos pedidos coletivos fechados
     */
    List<CollectiveOrder> findTop10ByStatusOrderByClosedAtDesc(CollectiveOrderStatus status);

    /**
     * Busca valor médio de pedidos coletivos por fornecedor
     */
    @Query("SELECT AVG(co.currentValue) FROM CollectiveOrder co WHERE co.supplier = :supplier AND co.status NOT IN ('CANCELLED')")
    BigDecimal getAverageOrderValueBySupplier(@Param("supplier") Supplier supplier);
}
