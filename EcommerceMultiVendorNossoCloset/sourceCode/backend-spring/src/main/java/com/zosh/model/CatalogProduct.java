package com.zosh.model;

import com.zosh.domain.ProductStatus;
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
 * Entidade CatalogProduct - Produtos dos catálogos dos fornecedores
 * Substitui a entidade Product para se adequar ao novo modelo de negócio
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Entity
@Table(name = "catalog_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Título é obrigatório")
    @Size(min = 2, max = 200, message = "Título deve ter entre 2 e 200 caracteres")
    private String title;

    @Column(nullable = false, length = 1000)
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 1000, message = "Descrição deve ter entre 10 e 1000 caracteres")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @NotNull(message = "Fornecedor é obrigatório")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id", nullable = false)
    @NotNull(message = "Catálogo é obrigatório")
    private Catalog catalog;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Código de referência é obrigatório")
    @Size(min = 1, max = 100, message = "Código de referência deve ter entre 1 e 100 caracteres")
    private String referenceCode; // Código de referência do fornecedor

    @ElementCollection
    @CollectionTable(name = "catalog_product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 500)
    private List<String> images = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "catalog_product_colors", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "color", length = 50)
    private List<String> availableColors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "catalog_product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size", length = 30)
    private List<String> availableSizes = new ArrayList<>();

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Categoria é obrigatória")
    @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
    private String category;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Subcategoria é obrigatória")
    @Size(max = 50, message = "Subcategoria deve ter no máximo 50 caracteres")
    private String subcategory;

    @Column(length = 100)
    @Size(max = 100, message = "Material deve ter no máximo 100 caracteres")
    private String material;

    @Column(length = 100)
    @Size(max = 100, message = "Marca deve ter no máximo 100 caracteres")
    private String brand;

    @Column(length = 50)
    @Size(max = 50, message = "Gênero deve ter no máximo 50 caracteres")
    private String gender; // Ex: "Masculino", "Feminino", "Unissex"

    @Column(length = 50)
    @Size(max = 50, message = "Faixa etária deve ter no máximo 50 caracteres")
    private String ageGroup; // Ex: "Adulto", "Infantil", "Jovem"

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Preço do fornecedor é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço do fornecedor deve ser maior que zero")
    private BigDecimal supplierPrice; // Preço do fornecedor

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    private BigDecimal sellingPrice; // Preço final para o cliente

    @Column(precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Percentual de desconto não pode ser negativo")
    @DecimalMax(value = "100.00", message = "Percentual de desconto não pode exceder 100%")
    private BigDecimal discountPercentage;

    @Column(precision = 10, scale = 2)
    private BigDecimal promotionalPrice; // Preço promocional temporário

    @Column
    private LocalDateTime promotionStartDate;

    @Column
    private LocalDateTime promotionEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Column
    private Integer viewCount = 0; // Quantas vezes foi visualizado

    @Column
    private Integer favoriteCount = 0; // Quantas vezes foi favoritado

    @Column
    private Integer orderCount = 0; // Quantas vezes foi pedido

    @ElementCollection
    @CollectionTable(name = "catalog_product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag", length = 30)
    private List<String> tags = new ArrayList<>();

    @Column(length = 500)
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observations;

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
    public void addImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.images.add(imageUrl.trim());
        }
    }

    public void removeImage(String imageUrl) {
        this.images.remove(imageUrl);
    }

    public void addColor(String color) {
        if (color != null && !color.trim().isEmpty()) {
            String normalizedColor = color.trim().toLowerCase();
            if (!this.availableColors.contains(normalizedColor)) {
                this.availableColors.add(normalizedColor);
            }
        }
    }

    public void removeColor(String color) {
        if (color != null) {
            this.availableColors.remove(color.toLowerCase());
        }
    }

    public void addSize(String size) {
        if (size != null && !size.trim().isEmpty()) {
            String normalizedSize = size.trim().toUpperCase();
            if (!this.availableSizes.contains(normalizedSize)) {
                this.availableSizes.add(normalizedSize);
            }
        }
    }

    public void removeSize(String size) {
        if (size != null) {
            this.availableSizes.remove(size.toUpperCase());
        }
    }

    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            String normalizedTag = tag.trim().toLowerCase();
            if (!this.tags.contains(normalizedTag)) {
                this.tags.add(normalizedTag);
            }
        }
    }

    public void removeTag(String tag) {
        if (tag != null) {
            this.tags.remove(tag.toLowerCase());
        }
    }

    public boolean isAvailable() {
        return this.status == ProductStatus.AVAILABLE;
    }

    public boolean isDiscontinued() {
        return this.status == ProductStatus.DISCONTINUED;
    }

    public boolean isOutOfStock() {
        return this.status == ProductStatus.OUT_OF_STOCK;
    }

    public void markAsAvailable() {
        this.status = ProductStatus.AVAILABLE;
    }

    public void markAsOutOfStock() {
        this.status = ProductStatus.OUT_OF_STOCK;
    }

    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void incrementFavoriteCount() {
        this.favoriteCount = (this.favoriteCount == null ? 0 : this.favoriteCount) + 1;
    }

    public void decrementFavoriteCount() {
        this.favoriteCount = Math.max(0, (this.favoriteCount == null ? 0 : this.favoriteCount) - 1);
    }

    public void incrementOrderCount() {
        this.orderCount = (this.orderCount == null ? 0 : this.orderCount) + 1;
    }

    /**
     * Verifica se há promoção ativa
     */
    public boolean hasActivePromotion() {
        if (promotionalPrice == null || promotionStartDate == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(promotionStartDate)) {
            return false;
        }
        
        if (promotionEndDate != null && now.isAfter(promotionEndDate)) {
            return false;
        }
        
        return promotionalPrice.compareTo(sellingPrice) < 0;
    }

    /**
     * Retorna o preço efetivo (promocional se ativo, senão o preço normal)
     */
    public BigDecimal getEffectivePrice() {
        return hasActivePromotion() ? promotionalPrice : sellingPrice;
    }

    /**
     * Calcula a margem de lucro em relação ao preço do fornecedor
     */
    public BigDecimal calculateProfitMargin() {
        if (supplierPrice == null || supplierPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal effectivePrice = getEffectivePrice();
        BigDecimal profit = effectivePrice.subtract(supplierPrice);
        
        return profit.divide(supplierPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Verifica se o produto tem imagens
     */
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    /**
     * Retorna a primeira imagem como imagem principal
     */
    public String getPrimaryImage() {
        return hasImages() ? images.get(0) : null;
    }

    /**
     * Verifica se a cor está disponível
     */
    public boolean hasColor(String color) {
        if (color == null || availableColors == null) {
            return false;
        }
        return availableColors.contains(color.toLowerCase());
    }

    /**
     * Verifica se o tamanho está disponível
     */
    public boolean hasSize(String size) {
        if (size == null || availableSizes == null) {
            return false;
        }
        return availableSizes.contains(size.toUpperCase());
    }

    /**
     * Retorna uma descrição resumida do produto
     */
    public String getShortDescription() {
        if (description == null) {
            return "";
        }
        
        if (description.length() <= 100) {
            return description;
        }
        
        return description.substring(0, 97) + "...";
    }
}
