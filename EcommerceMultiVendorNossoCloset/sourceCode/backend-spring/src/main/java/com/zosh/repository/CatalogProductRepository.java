package com.zosh.repository;

import com.zosh.domain.ProductStatus;
import com.zosh.model.Catalog;
import com.zosh.model.CatalogProduct;
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

/**
 * Repositório para entidade CatalogProduct
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Repository
public interface CatalogProductRepository extends JpaRepository<CatalogProduct, Long> {

    /**
     * Busca produtos por catálogo
     */
    List<CatalogProduct> findByCatalogOrderByCreatedAtDesc(Catalog catalog);

    /**
     * Busca produtos por catálogo paginados
     */
    Page<CatalogProduct> findByCatalog(Catalog catalog, Pageable pageable);

    /**
     * Busca produtos por fornecedor
     */
    List<CatalogProduct> findBySupplierOrderByCreatedAtDesc(Supplier supplier);

    /**
     * Busca produtos por status
     */
    List<CatalogProduct> findByStatusOrderByCreatedAtDesc(ProductStatus status);

    /**
     * Busca produtos disponíveis
     */
    List<CatalogProduct> findByStatusOrderByViewCountDesc(ProductStatus status);

    /**
     * Busca produtos por categoria
     */
    List<CatalogProduct> findByCategoryOrderByCreatedAtDesc(String category);

    /**
     * Busca produtos por categoria e subcategoria
     */
    List<CatalogProduct> findByCategoryAndSubcategoryOrderByCreatedAtDesc(String category, String subcategory);

    /**
     * Busca produtos por marca
     */
    List<CatalogProduct> findByBrandContainingIgnoreCaseOrderByCreatedAtDesc(String brand);

    /**
     * Busca produtos por código de referência
     */
    List<CatalogProduct> findByReferenceCodeContainingIgnoreCaseOrderByCreatedAtDesc(String referenceCode);

    /**
     * Busca produtos por faixa de preço
     */
    List<CatalogProduct> findBySellingPriceBetweenAndStatusOrderBySellingPriceAsc(BigDecimal minPrice, BigDecimal maxPrice, ProductStatus status);

    /**
     * Busca produtos com promoção ativa
     */
    @Query("SELECT p FROM CatalogProduct p WHERE p.promotionalPrice IS NOT NULL AND p.promotionalPrice < p.sellingPrice AND " +
           "p.promotionStartDate <= :now AND (p.promotionEndDate IS NULL OR p.promotionEndDate > :now) AND p.status = 'AVAILABLE' " +
           "ORDER BY ((p.sellingPrice - p.promotionalPrice) / p.sellingPrice) DESC")
    List<CatalogProduct> findProductsWithActivePromotion(@Param("now") LocalDateTime now);

    /**
     * Busca produtos por cor disponível
     */
    @Query("SELECT p FROM CatalogProduct p WHERE LOWER(:color) MEMBER OF p.availableColors AND p.status = 'AVAILABLE' ORDER BY p.createdAt DESC")
    List<CatalogProduct> findByColor(@Param("color") String color);

    /**
     * Busca produtos por tamanho disponível
     */
    @Query("SELECT p FROM CatalogProduct p WHERE UPPER(:size) MEMBER OF p.availableSizes AND p.status = 'AVAILABLE' ORDER BY p.createdAt DESC")
    List<CatalogProduct> findBySize(@Param("size") String size);

    /**
     * Busca produtos por tag
     */
    @Query("SELECT p FROM CatalogProduct p WHERE LOWER(:tag) MEMBER OF p.tags AND p.status = 'AVAILABLE' ORDER BY p.createdAt DESC")
    List<CatalogProduct> findByTag(@Param("tag") String tag);

    /**
     * Busca produtos com filtros avançados
     */
    @Query("SELECT p FROM CatalogProduct p WHERE " +
           "(:supplierId IS NULL OR p.supplier.id = :supplierId) AND " +
           "(:catalogId IS NULL OR p.catalog.id = :catalogId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:category IS NULL OR LOWER(p.category) = LOWER(:category)) AND " +
           "(:subcategory IS NULL OR LOWER(p.subcategory) = LOWER(:subcategory)) AND " +
           "(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
           "(:material IS NULL OR LOWER(p.material) LIKE LOWER(CONCAT('%', :material, '%'))) AND " +
           "(:gender IS NULL OR LOWER(p.gender) = LOWER(:gender)) AND " +
           "(:color IS NULL OR LOWER(:color) MEMBER OF p.availableColors) AND " +
           "(:size IS NULL OR UPPER(:size) MEMBER OF p.availableSizes) AND " +
           "(:minPrice IS NULL OR p.sellingPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.sellingPrice <= :maxPrice) AND " +
           "(:hasPromotion IS NULL OR " +
           " (:hasPromotion = true AND p.promotionalPrice IS NOT NULL AND p.promotionalPrice < p.sellingPrice AND " +
           "  p.promotionStartDate <= :now AND (p.promotionEndDate IS NULL OR p.promotionEndDate > :now)) OR " +
           " (:hasPromotion = false AND (p.promotionalPrice IS NULL OR p.promotionalPrice >= p.sellingPrice OR " +
           "  p.promotionStartDate > :now OR (p.promotionEndDate IS NOT NULL AND p.promotionEndDate <= :now)))) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(p.referenceCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CatalogProduct> findWithFilters(@Param("supplierId") Long supplierId,
                                        @Param("catalogId") Long catalogId,
                                        @Param("status") ProductStatus status,
                                        @Param("category") String category,
                                        @Param("subcategory") String subcategory,
                                        @Param("brand") String brand,
                                        @Param("material") String material,
                                        @Param("gender") String gender,
                                        @Param("color") String color,
                                        @Param("size") String size,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        @Param("hasPromotion") Boolean hasPromotion,
                                        @Param("searchTerm") String searchTerm,
                                        @Param("now") LocalDateTime now,
                                        Pageable pageable);

    /**
     * Conta produtos por status
     */
    long countByStatus(ProductStatus status);

    /**
     * Conta produtos por catálogo
     */
    long countByCatalog(Catalog catalog);

    /**
     * Conta produtos por fornecedor
     */
    long countBySupplier(Supplier supplier);

    /**
     * Busca produtos mais visualizados
     */
    @Query("SELECT p FROM CatalogProduct p WHERE p.status = 'AVAILABLE' ORDER BY p.viewCount DESC, p.createdAt DESC")
    List<CatalogProduct> findMostViewed(Pageable pageable);

    /**
     * Busca produtos mais favoritados
     */
    @Query("SELECT p FROM CatalogProduct p WHERE p.status = 'AVAILABLE' ORDER BY p.favoriteCount DESC, p.createdAt DESC")
    List<CatalogProduct> findMostFavorited(Pageable pageable);

    /**
     * Busca produtos mais pedidos
     */
    @Query("SELECT p FROM CatalogProduct p WHERE p.status = 'AVAILABLE' ORDER BY p.orderCount DESC, p.createdAt DESC")
    List<CatalogProduct> findMostOrdered(Pageable pageable);

    /**
     * Busca produtos similares (mesma categoria e subcategoria)
     */
    @Query("SELECT p FROM CatalogProduct p WHERE p.category = :category AND p.subcategory = :subcategory AND " +
           "p.id != :excludeId AND p.status = 'AVAILABLE' ORDER BY p.viewCount DESC")
    List<CatalogProduct> findSimilarProducts(@Param("category") String category, 
                                           @Param("subcategory") String subcategory, 
                                           @Param("excludeId") Long excludeId, 
                                           Pageable pageable);

    /**
     * Busca produtos do mesmo fornecedor
     */
    @Query("SELECT p FROM CatalogProduct p WHERE p.supplier = :supplier AND p.id != :excludeId AND p.status = 'AVAILABLE' ORDER BY p.createdAt DESC")
    List<CatalogProduct> findOtherProductsBySupplier(@Param("supplier") Supplier supplier, 
                                                    @Param("excludeId") Long excludeId, 
                                                    Pageable pageable);

    /**
     * Busca todas as categorias distintas
     */
    @Query("SELECT DISTINCT p.category FROM CatalogProduct p WHERE p.status = 'AVAILABLE' ORDER BY p.category")
    List<String> findDistinctCategories();

    /**
     * Busca subcategorias de uma categoria
     */
    @Query("SELECT DISTINCT p.subcategory FROM CatalogProduct p WHERE p.category = :category AND p.status = 'AVAILABLE' ORDER BY p.subcategory")
    List<String> findDistinctSubcategoriesByCategory(@Param("category") String category);

    /**
     * Busca todas as marcas distintas
     */
    @Query("SELECT DISTINCT p.brand FROM CatalogProduct p WHERE p.brand IS NOT NULL AND p.status = 'AVAILABLE' ORDER BY p.brand")
    List<String> findDistinctBrands();

    /**
     * Busca todas as cores disponíveis
     */
    @Query("SELECT DISTINCT c FROM CatalogProduct p JOIN p.availableColors c WHERE p.status = 'AVAILABLE' ORDER BY c")
    List<String> findDistinctColors();

    /**
     * Busca todos os tamanhos disponíveis
     */
    @Query("SELECT DISTINCT s FROM CatalogProduct p JOIN p.availableSizes s WHERE p.status = 'AVAILABLE' ORDER BY s")
    List<String> findDistinctSizes();

    /**
     * Busca produtos criados em um período
     */
    List<CatalogProduct> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Busca produtos com preço baixo (menor que a média)
     */
    @Query("SELECT p FROM CatalogProduct p WHERE p.sellingPrice < (SELECT AVG(p2.sellingPrice) FROM CatalogProduct p2 WHERE p2.status = 'AVAILABLE') AND p.status = 'AVAILABLE' ORDER BY p.sellingPrice ASC")
    List<CatalogProduct> findBelowAveragePrice(Pageable pageable);

    /**
     * Verifica se existe produto com mesmo código de referência no fornecedor
     */
    boolean existsBySupplierAndReferenceCodeIgnoreCaseAndIdNot(Supplier supplier, String referenceCode, Long id);

    /**
     * Busca faixa de preços (min e max)
     */
    @Query("SELECT MIN(p.sellingPrice), MAX(p.sellingPrice) FROM CatalogProduct p WHERE p.status = 'AVAILABLE'")
    List<Object[]> findPriceRange();

    /**
     * Busca estatísticas de produtos por categoria
     */
    @Query("SELECT p.category, COUNT(p), AVG(p.sellingPrice), MIN(p.sellingPrice), MAX(p.sellingPrice) " +
           "FROM CatalogProduct p " +
           "WHERE p.status = 'AVAILABLE' " +
           "GROUP BY p.category " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> getProductStatisticsByCategory();

    /**
     * Busca produtos novos (últimos 30 dias)
     */
    @Query("SELECT p FROM CatalogProduct p WHERE p.createdAt >= :cutoffDate AND p.status = 'AVAILABLE' ORDER BY p.createdAt DESC")
    List<CatalogProduct> findNewProducts(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Atualiza contador de visualizações
     */
    @Query("UPDATE CatalogProduct p SET p.viewCount = p.viewCount + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") Long productId);

    /**
     * Atualiza contador de favoritos
     */
    @Query("UPDATE CatalogProduct p SET p.favoriteCount = p.favoriteCount + 1 WHERE p.id = :productId")
    void incrementFavoriteCount(@Param("productId") Long productId);

    /**
     * Atualiza contador de pedidos
     */
    @Query("UPDATE CatalogProduct p SET p.orderCount = p.orderCount + 1 WHERE p.id = :productId")
    void incrementOrderCount(@Param("productId") Long productId);
}
