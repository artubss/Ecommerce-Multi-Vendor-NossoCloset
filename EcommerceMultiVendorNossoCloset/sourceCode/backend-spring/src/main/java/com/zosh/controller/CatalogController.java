package com.zosh.controller;

import com.zosh.domain.CatalogStatus;
import com.zosh.domain.CatalogType;
import com.zosh.dto.CatalogRequestDto;
import com.zosh.dto.CatalogResponseDto;
import com.zosh.exception.GlobalExceptionHandler;
import com.zosh.model.Catalog;
import com.zosh.model.Supplier;
import com.zosh.repository.CatalogRepository;
import com.zosh.repository.SupplierRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de catálogos
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Slf4j
@RestController
@RequestMapping("/api/catalogs")
@Tag(name = "Catalogs", description = "Operações de gerenciamento de catálogos")
public class CatalogController {

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    // ===== OPERAÇÕES CRUD =====

    @PostMapping
    @Operation(summary = "Criar novo catálogo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<?> createCatalog(
            @Valid @RequestBody CatalogRequestDto catalogDto,
            Authentication authentication) {
        
        try {
            // Buscar fornecedor
            Optional<Supplier> supplier = supplierRepository.findById(catalogDto.getSupplierId());
            if (supplier.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Fornecedor não encontrado");
            }

            // Verificar se já existe catálogo com mesmo nome para o fornecedor
            if (catalogRepository.existsBySupplierAndNameIgnoreCaseAndIdNot(
                    supplier.get(), catalogDto.getName(), -1L)) {
                throw new GlobalExceptionHandler.BusinessConflictException(
                    "Já existe um catálogo com este nome para este fornecedor");
            }

            Catalog catalog = new Catalog();
            catalog.setSupplier(supplier.get());
            catalog.setName(catalogDto.getName());
            catalog.setDescription(catalogDto.getDescription());
            catalog.setFileUrl(catalogDto.getFileUrl());
            catalog.setType(catalogDto.getType() != null ? catalogDto.getType() : CatalogType.PDF);
            catalog.setValidFrom(catalogDto.getValidFrom());
            catalog.setValidUntil(catalogDto.getValidUntil());
            catalog.setSeason(catalogDto.getSeason());
            catalog.setCreatedBy(authentication.getName());
            catalog.setUpdatedBy(authentication.getName());

            // Adicionar URLs de imagens se fornecidas
            if (catalogDto.getImageUrls() != null) {
                catalogDto.getImageUrls().forEach(catalog::addImageUrl);
            }

            // Adicionar tags se fornecidas
            if (catalogDto.getTags() != null) {
                catalogDto.getTags().forEach(catalog::addTag);
            }

            Catalog savedCatalog = catalogRepository.save(catalog);
            CatalogResponseDto responseDto = toCatalogResponseDto(savedCatalog);

            log.info("Catálogo criado com sucesso: {} por {}", savedCatalog.getName(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Catálogo criado com sucesso");
            response.put("catalog", responseDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException | GlobalExceptionHandler.BusinessConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao criar catálogo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar catálogo por ID")
    public ResponseEntity<?> getCatalogById(@PathVariable Long id) {
        try {
            Optional<Catalog> catalog = catalogRepository.findById(id);
            
            if (catalog.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Catálogo não encontrado");
            }

            // Incrementar contador de visualizações
            catalogRepository.incrementViewCount(id);

            CatalogResponseDto responseDto = toCatalogResponseDto(catalog.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("catalog", responseDto);

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar catálogo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar catálogo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<?> updateCatalog(
            @PathVariable Long id,
            @Valid @RequestBody CatalogRequestDto catalogDto,
            Authentication authentication) {
        
        try {
            Optional<Catalog> existingCatalog = catalogRepository.findById(id);
            
            if (existingCatalog.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Catálogo não encontrado");
            }

            // Buscar fornecedor
            Optional<Supplier> supplier = supplierRepository.findById(catalogDto.getSupplierId());
            if (supplier.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Fornecedor não encontrado");
            }

            // Verificar se já existe catálogo com mesmo nome para o fornecedor (excluindo o atual)
            if (catalogRepository.existsBySupplierAndNameIgnoreCaseAndIdNot(
                    supplier.get(), catalogDto.getName(), id)) {
                throw new GlobalExceptionHandler.BusinessConflictException(
                    "Já existe um catálogo com este nome para este fornecedor");
            }

            Catalog catalog = existingCatalog.get();
            catalog.setSupplier(supplier.get());
            catalog.setName(catalogDto.getName());
            catalog.setDescription(catalogDto.getDescription());
            catalog.setFileUrl(catalogDto.getFileUrl());
            catalog.setType(catalogDto.getType() != null ? catalogDto.getType() : catalog.getType());
            catalog.setValidFrom(catalogDto.getValidFrom());
            catalog.setValidUntil(catalogDto.getValidUntil());
            catalog.setSeason(catalogDto.getSeason());
            catalog.setUpdatedBy(authentication.getName());

            // Atualizar URLs de imagens
            catalog.getImageUrls().clear();
            if (catalogDto.getImageUrls() != null) {
                catalogDto.getImageUrls().forEach(catalog::addImageUrl);
            }

            // Atualizar tags
            catalog.getTags().clear();
            if (catalogDto.getTags() != null) {
                catalogDto.getTags().forEach(catalog::addTag);
            }

            Catalog savedCatalog = catalogRepository.save(catalog);
            CatalogResponseDto responseDto = toCatalogResponseDto(savedCatalog);

            log.info("Catálogo atualizado com sucesso: {} por {}", savedCatalog.getName(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Catálogo atualizado com sucesso");
            response.put("catalog", responseDto);

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException | GlobalExceptionHandler.BusinessConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao atualizar catálogo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    // ===== OPERAÇÕES DE LISTAGEM =====

    @GetMapping
    @Operation(summary = "Listar catálogos com filtros")
    public ResponseEntity<?> getCatalogs(
            @Parameter(description = "ID do fornecedor") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "Status") @RequestParam(required = false) CatalogStatus status,
            @Parameter(description = "Tipo") @RequestParam(required = false) CatalogType type,
            @Parameter(description = "Temporada") @RequestParam(required = false) String season,
            @Parameter(description = "Tag") @RequestParam(required = false) String tag,
            @Parameter(description = "Termo de busca") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Apenas válidos") @RequestParam(defaultValue = "false") boolean validOnly,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<Catalog> catalogsPage = catalogRepository.findWithFilters(
                supplierId, status, type, season, tag, searchTerm, validOnly, LocalDateTime.now(), pageable
            );

            List<CatalogResponseDto> catalogs = catalogsPage.getContent().stream()
                    .map(this::toCatalogResponseDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("catalogs", catalogs);
            response.put("totalElements", catalogsPage.getTotalElements());
            response.put("totalPages", catalogsPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("hasNext", catalogsPage.hasNext());
            response.put("hasPrevious", catalogsPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar catálogos: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/public")
    @Operation(summary = "Listar catálogos públicos (ativos e válidos)")
    public ResponseEntity<?> getPublicCatalogs(
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "12") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Catalog> catalogsPage = catalogRepository.findPublicCatalogs(LocalDateTime.now(), pageable);

            List<CatalogResponseDto> catalogs = catalogsPage.getContent().stream()
                    .map(this::toCatalogResponseDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("catalogs", catalogs);
            response.put("totalElements", catalogsPage.getTotalElements());
            response.put("totalPages", catalogsPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar catálogos públicos: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Listar catálogos de um fornecedor")
    public ResponseEntity<?> getCatalogsBySupplier(@PathVariable Long supplierId) {
        try {
            Optional<Supplier> supplier = supplierRepository.findById(supplierId);
            if (supplier.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Fornecedor não encontrado");
            }

            List<Catalog> catalogs = catalogRepository.findValidBySupplier(supplier.get(), LocalDateTime.now());
            List<CatalogResponseDto> catalogDtos = catalogs.stream()
                    .map(this::toCatalogResponseDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("catalogs", catalogDtos);
            response.put("total", catalogDtos.size());
            response.put("supplier", Map.of(
                "id", supplier.get().getId(),
                "name", supplier.get().getName()
            ));

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao listar catálogos do fornecedor: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    // ===== OPERAÇÕES DE STATUS =====

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar catálogo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateCatalog(@PathVariable Long id, Authentication authentication) {
        return updateCatalogStatus(id, CatalogStatus.ACTIVE, authentication);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar catálogo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateCatalog(@PathVariable Long id, Authentication authentication) {
        return updateCatalogStatus(id, CatalogStatus.INACTIVE, authentication);
    }

    private ResponseEntity<?> updateCatalogStatus(Long id, CatalogStatus newStatus, Authentication authentication) {
        try {
            Optional<Catalog> catalog = catalogRepository.findById(id);
            
            if (catalog.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Catálogo não encontrado");
            }

            Catalog existingCatalog = catalog.get();
            existingCatalog.setStatus(newStatus);
            existingCatalog.setUpdatedBy(authentication.getName());

            Catalog savedCatalog = catalogRepository.save(existingCatalog);
            CatalogResponseDto responseDto = toCatalogResponseDto(savedCatalog);

            log.info("Status do catálogo {} alterado para {} por {}", 
                    savedCatalog.getName(), newStatus, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Status do catálogo atualizado com sucesso");
            response.put("catalog", responseDto);

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao atualizar status do catálogo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    // ===== MÉTODOS UTILITÁRIOS =====

    private CatalogResponseDto toCatalogResponseDto(Catalog catalog) {
        CatalogResponseDto dto = new CatalogResponseDto();
        dto.setId(catalog.getId());
        dto.setName(catalog.getName());
        dto.setDescription(catalog.getDescription());
        dto.setFileUrl(catalog.getFileUrl());
        dto.setImageUrls(catalog.getImageUrls());
        dto.setType(catalog.getType());
        dto.setStatus(catalog.getStatus());
        dto.setValidFrom(catalog.getValidFrom());
        dto.setValidUntil(catalog.getValidUntil());
        dto.setSeason(catalog.getSeason());
        dto.setTags(catalog.getTags());
        dto.setViewCount(catalog.getViewCount());
        dto.setDownloadCount(catalog.getDownloadCount());
        dto.setCreatedAt(catalog.getCreatedAt());
        dto.setUpdatedAt(catalog.getUpdatedAt());
        dto.setCreatedBy(catalog.getCreatedBy());
        dto.setUpdatedBy(catalog.getUpdatedBy());

        // Relacionamentos
        if (catalog.getSupplier() != null) {
            dto.setSupplier(new CatalogResponseDto.SupplierSummaryDto(
                catalog.getSupplier().getId(),
                catalog.getSupplier().getName(),
                catalog.getSupplier().getContactName()
            ));
        }

        // Campos calculados
        dto.setProductCount(catalog.getProductCount());
        dto.setActive(catalog.isActive());
        dto.setValid(catalog.isValid());
        dto.setHasContent(catalog.hasContent());

        return dto;
    }
}
