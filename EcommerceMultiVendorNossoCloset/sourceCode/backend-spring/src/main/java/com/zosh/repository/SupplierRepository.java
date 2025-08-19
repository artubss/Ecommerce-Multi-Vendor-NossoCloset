package com.zosh.repository;

import com.zosh.domain.SupplierStatus;
import com.zosh.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositório para entidade Supplier
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * Busca fornecedores por status
     */
    List<Supplier> findByStatus(SupplierStatus status);

    /**
     * Busca fornecedores ativos
     */
    List<Supplier> findByStatusOrderByNameAsc(SupplierStatus status);

    /**
     * Busca fornecedores por categoria
     */
    @Query("SELECT s FROM Supplier s WHERE :category MEMBER OF s.categories AND s.status = :status")
    List<Supplier> findByCategoryAndStatus(@Param("category") String category, @Param("status") SupplierStatus status);

    /**
     * Busca fornecedores por rating mínimo
     */
    List<Supplier> findByPerformanceRatingGreaterThanEqualAndStatus(Integer minRating, SupplierStatus status);

    /**
     * Busca fornecedores por valor mínimo de pedido
     */
    List<Supplier> findByMinimumOrderValueLessThanEqualAndStatus(BigDecimal maxMinimumValue, SupplierStatus status);

    /**
     * Busca fornecedores por prazo de entrega máximo
     */
    List<Supplier> findByDeliveryTimeDaysLessThanEqualAndStatus(Integer maxDays, SupplierStatus status);

    /**
     * Busca fornecedores por nome (case insensitive)
     */
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.status = :status")
    List<Supplier> findByNameContainingIgnoreCaseAndStatus(@Param("name") String name, @Param("status") SupplierStatus status);

    /**
     * Busca fornecedores com WhatsApp específico
     */
    Supplier findByWhatsappAndStatus(String whatsapp, SupplierStatus status);

    /**
     * Busca fornecedores com email específico
     */
    Supplier findByEmailAndStatus(String email, SupplierStatus status);

    /**
     * Busca fornecedores paginados com filtros
     */
    @Query("SELECT s FROM Supplier s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:category IS NULL OR :category MEMBER OF s.categories) AND " +
           "(:minRating IS NULL OR s.performanceRating >= :minRating) AND " +
           "(:maxMinimumValue IS NULL OR s.minimumOrderValue <= :maxMinimumValue) AND " +
           "(:maxDeliveryDays IS NULL OR s.deliveryTimeDays <= :maxDeliveryDays) AND " +
           "(:searchTerm IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(s.contactName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Supplier> findWithFilters(@Param("status") SupplierStatus status,
                                  @Param("category") String category,
                                  @Param("minRating") Integer minRating,
                                  @Param("maxMinimumValue") BigDecimal maxMinimumValue,
                                  @Param("maxDeliveryDays") Integer maxDeliveryDays,
                                  @Param("searchTerm") String searchTerm,
                                  Pageable pageable);

    /**
     * Conta fornecedores por status
     */
    long countByStatus(SupplierStatus status);

    /**
     * Busca top fornecedores por rating
     */
    @Query("SELECT s FROM Supplier s WHERE s.status = :status ORDER BY s.performanceRating DESC, s.name ASC")
    List<Supplier> findTopRatedSuppliers(@Param("status") SupplierStatus status, Pageable pageable);

    /**
     * Busca fornecedores com delivery mais rápido
     */
    @Query("SELECT s FROM Supplier s WHERE s.status = :status ORDER BY s.deliveryTimeDays ASC, s.name ASC")
    List<Supplier> findFastestDeliverySuppliers(@Param("status") SupplierStatus status, Pageable pageable);

    /**
     * Busca fornecedores com menor valor mínimo
     */
    @Query("SELECT s FROM Supplier s WHERE s.status = :status ORDER BY s.minimumOrderValue ASC, s.name ASC")
    List<Supplier> findLowestMinimumValueSuppliers(@Param("status") SupplierStatus status, Pageable pageable);

    /**
     * Verifica se existe fornecedor com WhatsApp específico (excluindo um ID)
     */
    boolean existsByWhatsappAndIdNot(String whatsapp, Long id);

    /**
     * Verifica se existe fornecedor com email específico (excluindo um ID)
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Busca todas as categorias distintas dos fornecedores ativos
     */
    @Query("SELECT DISTINCT c FROM Supplier s JOIN s.categories c WHERE s.status = :status ORDER BY c")
    List<String> findDistinctCategoriesByStatus(@Param("status") SupplierStatus status);
}
