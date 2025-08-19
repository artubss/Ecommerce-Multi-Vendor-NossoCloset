package com.zosh.model;

import com.zosh.domain.CatalogStatus;
import com.zosh.domain.CatalogType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Catalog - Catálogos dos fornecedores
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Entity
@Table(name = "catalogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @NotNull(message = "Fornecedor é obrigatório")
    private Supplier supplier;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nome do catálogo é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @Column(nullable = false, length = 500)
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 500, message = "Descrição deve ter entre 10 e 500 caracteres")
    private String description;

    @Column(length = 500)
    @Size(max = 500, message = "URL do arquivo deve ter no máximo 500 caracteres")
    private String fileUrl; // URL do PDF no S3

    @ElementCollection
    @CollectionTable(name = "catalog_images", joinColumns = @JoinColumn(name = "catalog_id"))
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CatalogType type = CatalogType.PDF;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CatalogStatus status = CatalogStatus.ACTIVE;

    @Column(nullable = false)
    @NotNull(message = "Data de início de validade é obrigatória")
    private LocalDateTime validFrom;

    @Column
    private LocalDateTime validUntil;

    @Column(length = 50)
    private String season; // Ex: "Verão 2024", "Inverno 2024"

    @ElementCollection
    @CollectionTable(name = "catalog_tags", joinColumns = @JoinColumn(name = "catalog_id"))
    @Column(name = "tag", length = 30)
    private List<String> tags = new ArrayList<>();

    @Column
    private Integer viewCount = 0;

    @Column
    private Integer downloadCount = 0;

    // Relacionamentos
    @OneToMany(mappedBy = "catalog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CatalogProduct> products = new ArrayList<>();

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
    public void addImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.imageUrls.add(imageUrl.trim());
        }
    }

    public void removeImageUrl(String imageUrl) {
        this.imageUrls.remove(imageUrl);
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

    public boolean isActive() {
        return this.status == CatalogStatus.ACTIVE;
    }

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        
        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }
        
        return isActive();
    }

    public void activate() {
        this.status = CatalogStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = CatalogStatus.INACTIVE;
    }

    public void expire() {
        this.status = CatalogStatus.EXPIRED;
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }

    /**
     * Verifica se o catálogo tem conteúdo (arquivo ou imagens)
     */
    public boolean hasContent() {
        return (fileUrl != null && !fileUrl.trim().isEmpty()) || 
               (imageUrls != null && !imageUrls.isEmpty());
    }

    /**
     * Retorna o número total de produtos no catálogo
     */
    public int getProductCount() {
        return products != null ? products.size() : 0;
    }
}
