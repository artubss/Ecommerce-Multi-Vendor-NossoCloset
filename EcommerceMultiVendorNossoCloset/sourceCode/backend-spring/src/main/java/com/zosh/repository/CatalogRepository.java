package com.zosh.repository;

import com.zosh.domain.CatalogStatus;
import com.zosh.domain.CatalogType;
import com.zosh.model.Catalog;
import com.zosh.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para entidade Catalog
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {

    /**
     * Busca catálogos por fornecedor
     */
    List<Catalog> findBySupplierOrderByCreatedAtDesc(Supplier supplier);

    /**
     * Busca catálogos ativos por fornecedor
     */
    List<Catalog> findBySupplierAndStatusOrderByCreatedAtDesc(Supplier supplier, CatalogStatus status);

    /**
     * Busca catálogos por status
     */
    List<Catalog> findByStatusOrderByCreatedAtDesc(CatalogStatus status);

    /**
     * Busca catálogos por status paginados
     */
    Page<Catalog> findByStatus(CatalogStatus status, Pageable pageable);

    /**
     * Busca catálogos por tipo
     */
    List<Catalog> findByTypeOrderByCreatedAtDesc(CatalogType type);

    /**
     * Busca catálogos ativos e válidos
     */
    @Query("SELECT c FROM Catalog c WHERE c.status = 'ACTIVE' AND " +
           "(c.validFrom IS NULL OR c.validFrom <= :now) AND " +
           "(c.validUntil IS NULL OR c.validUntil > :now) " +
           "ORDER BY c.createdAt DESC")
    List<Catalog> findActiveAndValid(@Param("now") LocalDateTime now);

    /**
     * Busca catálogos públicos (ativos e válidos) paginados
     */
    @Query("SELECT c FROM Catalog c WHERE c.status = 'ACTIVE' AND " +
           "(c.validFrom IS NULL OR c.validFrom <= :now) AND " +
           "(c.validUntil IS NULL OR c.validUntil > :now) " +
           "ORDER BY c.viewCount DESC, c.createdAt DESC")
    Page<Catalog> findPublicCatalogs(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Busca catálogos por fornecedor que estão válidos
     */
    @Query("SELECT c FROM Catalog c WHERE c.supplier = :supplier AND c.status = 'ACTIVE' AND " +
           "(c.validFrom IS NULL OR c.validFrom <= :now) AND " +
           "(c.validUntil IS NULL OR c.validUntil > :now) " +
           "ORDER BY c.createdAt DESC")
    List<Catalog> findValidBySupplier(@Param("supplier") Supplier supplier, @Param("now") LocalDateTime now);

    /**
     * Busca catálogos por temporada
     */
    List<Catalog> findBySeasonContainingIgnoreCaseOrderByCreatedAtDesc(String season);

    /**
     * Busca catálogos por tag
     */
    @Query("SELECT c FROM Catalog c WHERE :tag MEMBER OF c.tags ORDER BY c.createdAt DESC")
    List<Catalog> findByTag(@Param("tag") String tag);

    /**
     * Busca catálogos próximos ao vencimento
     */
    @Query("SELECT c FROM Catalog c WHERE c.status = 'ACTIVE' AND c.validUntil IS NOT NULL AND " +
           "c.validUntil BETWEEN :now AND :cutoff ORDER BY c.validUntil ASC")
    List<Catalog> findNearExpiration(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);

    /**
     * Busca catálogos expirados
     */
    @Query("SELECT c FROM Catalog c WHERE c.validUntil IS NOT NULL AND c.validUntil < :now AND c.status = 'ACTIVE' ORDER BY c.validUntil ASC")
    List<Catalog> findExpired(@Param("now") LocalDateTime now);

    /**
     * Busca catálogos com filtros avançados
     */
    @Query("SELECT c FROM Catalog c WHERE " +
           "(:supplierId IS NULL OR c.supplier.id = :supplierId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:season IS NULL OR LOWER(c.season) LIKE LOWER(CONCAT('%', :season, '%'))) AND " +
           "(:tag IS NULL OR :tag MEMBER OF c.tags) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:validOnly = false OR (c.status = 'ACTIVE' AND " +
           " (c.validFrom IS NULL OR c.validFrom <= :now) AND " +
           " (c.validUntil IS NULL OR c.validUntil > :now)))")
    Page<Catalog> findWithFilters(@Param("supplierId") Long supplierId,
                                 @Param("status") CatalogStatus status,
                                 @Param("type") CatalogType type,
                                 @Param("season") String season,
                                 @Param("tag") String tag,
                                 @Param("searchTerm") String searchTerm,
                                 @Param("validOnly") boolean validOnly,
                                 @Param("now") LocalDateTime now,
                                 Pageable pageable);

    /**
     * Conta catálogos por status
     */
    long countByStatus(CatalogStatus status);

    /**
     * Conta catálogos por fornecedor
     */
    long countBySupplier(Supplier supplier);

    /**
     * Conta catálogos ativos por fornecedor
     */
    long countBySupplierAndStatus(Supplier supplier, CatalogStatus status);

    /**
     * Busca catálogos mais visualizados
     */
    @Query("SELECT c FROM Catalog c WHERE c.status = 'ACTIVE' ORDER BY c.viewCount DESC, c.createdAt DESC")
    List<Catalog> findMostViewed(Pageable pageable);

    /**
     * Busca catálogos mais baixados
     */
    @Query("SELECT c FROM Catalog c WHERE c.status = 'ACTIVE' ORDER BY c.downloadCount DESC, c.createdAt DESC")
    List<Catalog> findMostDownloaded(Pageable pageable);

    /**
     * Busca todas as temporadas distintas
     */
    @Query("SELECT DISTINCT c.season FROM Catalog c WHERE c.season IS NOT NULL AND c.status = 'ACTIVE' ORDER BY c.season DESC")
    List<String> findDistinctSeasons();

    /**
     * Busca todas as tags distintas
     */
    @Query("SELECT DISTINCT t FROM Catalog c JOIN c.tags t WHERE c.status = 'ACTIVE' ORDER BY t")
    List<String> findDistinctTags();

    /**
     * Busca catálogos criados em um período
     */
    List<Catalog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Busca catálogos por nome (case insensitive)
     */
    List<Catalog> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);

    /**
     * Verifica se existe catálogo com mesmo nome para o fornecedor
     */
    boolean existsBySupplierAndNameIgnoreCaseAndIdNot(Supplier supplier, String name, Long id);

    /**
     * Busca catálogos que precisam de revisão
     */
    @Query("SELECT c FROM Catalog c WHERE c.status = 'PENDING_REVIEW' ORDER BY c.createdAt ASC")
    List<Catalog> findPendingReview();

    /**
     * Busca estatísticas de visualização por período
     */
    @Query("SELECT DATE(c.createdAt), COUNT(c), SUM(c.viewCount), SUM(c.downloadCount) " +
           "FROM Catalog c " +
           "WHERE c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(c.createdAt) " +
           "ORDER BY DATE(c.createdAt) DESC")
    List<Object[]> getViewStatisticsByPeriod(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Busca fornecedores mais ativos (com mais catálogos)
     */
    @Query("SELECT c.supplier, COUNT(c) as catalogCount " +
           "FROM Catalog c " +
           "WHERE c.status = 'ACTIVE' " +
           "GROUP BY c.supplier " +
           "ORDER BY catalogCount DESC")
    List<Object[]> getMostActiveSuppliersWithCatalogCount();

    /**
     * Atualiza contador de visualizações
     */
    @Query("UPDATE Catalog c SET c.viewCount = c.viewCount + 1 WHERE c.id = :catalogId")
    void incrementViewCount(@Param("catalogId") Long catalogId);

    /**
     * Atualiza contador de downloads
     */
    @Query("UPDATE Catalog c SET c.downloadCount = c.downloadCount + 1 WHERE c.id = :catalogId")
    void incrementDownloadCount(@Param("catalogId") Long catalogId);
}
