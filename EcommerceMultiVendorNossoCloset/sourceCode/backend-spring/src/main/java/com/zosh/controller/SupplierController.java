package com.zosh.controller;

import com.zosh.domain.SupplierStatus;
import com.zosh.dto.SupplierRequestDto;
import com.zosh.dto.SupplierResponseDto;
import com.zosh.model.Supplier;
import com.zosh.repository.SupplierRepository;
import com.zosh.service.DtoMapperService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller REST para gerenciamento de fornecedores
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Slf4j
@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Operações de gerenciamento de fornecedores")
public class SupplierController {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private DtoMapperService dtoMapper;

    // ===== OPERAÇÕES CRUD =====

    @PostMapping
    @Operation(summary = "Criar novo fornecedor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSupplier(
            @Valid @RequestBody SupplierRequestDto supplierDto,
            Authentication authentication) {
        
        try {
            // Validar WhatsApp único
            if (supplierRepository.existsByWhatsappAndIdNot(supplierDto.getWhatsapp(), -1L)) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "WhatsApp já está em uso por outro fornecedor")
                );
            }

            // Validar email único se fornecido
            if (supplierDto.getEmail() != null && !supplierDto.getEmail().isEmpty()) {
                if (supplierRepository.existsByEmailAndIdNot(supplierDto.getEmail(), -1L)) {
                    return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Email já está em uso por outro fornecedor")
                    );
                }
            }

            Supplier supplier = dtoMapper.toSupplierEntity(supplierDto);
            supplier.setCreatedBy(authentication.getName());
            supplier.setUpdatedBy(authentication.getName());

            Supplier savedSupplier = supplierRepository.save(supplier);
            SupplierResponseDto responseDto = dtoMapper.toSupplierDto(savedSupplier);

            log.info("Fornecedor criado com sucesso: {} por {}", savedSupplier.getName(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fornecedor criado com sucesso");
            response.put("supplier", responseDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Erro ao criar fornecedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar fornecedor por ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<?> getSupplierById(@PathVariable Long id) {
        try {
            Optional<Supplier> supplier = supplierRepository.findById(id);
            
            if (supplier.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Fornecedor não encontrado")
                );
            }

            SupplierResponseDto responseDto = dtoMapper.toSupplierDto(supplier.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("supplier", responseDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao buscar fornecedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fornecedor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequestDto supplierDto,
            Authentication authentication) {
        
        try {
            Optional<Supplier> existingSupplier = supplierRepository.findById(id);
            
            if (existingSupplier.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Fornecedor não encontrado")
                );
            }

            // Validar WhatsApp único (excluindo o próprio fornecedor)
            if (supplierRepository.existsByWhatsappAndIdNot(supplierDto.getWhatsapp(), id)) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "WhatsApp já está em uso por outro fornecedor")
                );
            }

            // Validar email único se fornecido (excluindo o próprio fornecedor)
            if (supplierDto.getEmail() != null && !supplierDto.getEmail().isEmpty()) {
                if (supplierRepository.existsByEmailAndIdNot(supplierDto.getEmail(), id)) {
                    return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Email já está em uso por outro fornecedor")
                    );
                }
            }

            Supplier supplier = existingSupplier.get();
            dtoMapper.updateSupplierFromDto(supplier, supplierDto);
            supplier.setUpdatedBy(authentication.getName());

            Supplier savedSupplier = supplierRepository.save(supplier);
            SupplierResponseDto responseDto = dtoMapper.toSupplierDto(savedSupplier);

            log.info("Fornecedor atualizado com sucesso: {} por {}", savedSupplier.getName(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fornecedor atualizado com sucesso");
            response.put("supplier", responseDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao atualizar fornecedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir fornecedor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<Supplier> supplier = supplierRepository.findById(id);
            
            if (supplier.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Fornecedor não encontrado")
                );
            }

            // Verificar se fornecedor tem pedidos ativos
            long activeOrders = supplierRepository.countBySupplier(supplier.get());
            if (activeOrders > 0) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Não é possível excluir fornecedor com pedidos ativos")
                );
            }

            supplierRepository.deleteById(id);

            log.info("Fornecedor excluído com sucesso: {} por {}", supplier.get().getName(), authentication.getName());

            return ResponseEntity.ok(Map.of("success", true, "message", "Fornecedor excluído com sucesso"));

        } catch (Exception e) {
            log.error("Erro ao excluir fornecedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    // ===== OPERAÇÕES DE LISTAGEM =====

    @GetMapping
    @Operation(summary = "Listar fornecedores com filtros e paginação")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<?> getSuppliers(
            @Parameter(description = "Status do fornecedor") @RequestParam(required = false) SupplierStatus status,
            @Parameter(description = "Categoria") @RequestParam(required = false) String category,
            @Parameter(description = "Rating mínimo") @RequestParam(required = false) Integer minRating,
            @Parameter(description = "Valor máximo mínimo") @RequestParam(required = false) BigDecimal maxMinimumValue,
            @Parameter(description = "Prazo máximo de entrega") @RequestParam(required = false) Integer maxDeliveryDays,
            @Parameter(description = "Termo de busca") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Ordenação") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "asc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<Supplier> suppliersPage = supplierRepository.findWithFilters(
                status, category, minRating, maxMinimumValue, maxDeliveryDays, searchTerm, pageable
            );

            List<SupplierResponseDto> suppliers = dtoMapper.toSupplierDtoList(suppliersPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suppliers", suppliers);
            response.put("totalElements", suppliersPage.getTotalElements());
            response.put("totalPages", suppliersPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("hasNext", suppliersPage.hasNext());
            response.put("hasPrevious", suppliersPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar fornecedores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @GetMapping("/active")
    @Operation(summary = "Listar fornecedores ativos")
    public ResponseEntity<?> getActiveSuppliers() {
        try {
            List<Supplier> activeSuppliers = supplierRepository.findByStatusOrderByNameAsc(SupplierStatus.ACTIVE);
            List<SupplierResponseDto> suppliers = dtoMapper.toSupplierDtoList(activeSuppliers);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suppliers", suppliers);
            response.put("total", suppliers.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar fornecedores ativos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    // ===== OPERAÇÕES DE STATUS =====

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar fornecedor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateSupplier(@PathVariable Long id, Authentication authentication) {
        return updateSupplierStatus(id, SupplierStatus.ACTIVE, authentication);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar fornecedor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateSupplier(@PathVariable Long id, Authentication authentication) {
        return updateSupplierStatus(id, SupplierStatus.INACTIVE, authentication);
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspender fornecedor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> suspendSupplier(@PathVariable Long id, Authentication authentication) {
        return updateSupplierStatus(id, SupplierStatus.SUSPENDED, authentication);
    }

    private ResponseEntity<?> updateSupplierStatus(Long id, SupplierStatus newStatus, Authentication authentication) {
        try {
            Optional<Supplier> supplier = supplierRepository.findById(id);
            
            if (supplier.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Fornecedor não encontrado")
                );
            }

            Supplier existingSupplier = supplier.get();
            existingSupplier.setStatus(newStatus);
            existingSupplier.setUpdatedBy(authentication.getName());

            Supplier savedSupplier = supplierRepository.save(existingSupplier);
            SupplierResponseDto responseDto = dtoMapper.toSupplierDto(savedSupplier);

            log.info("Status do fornecedor {} alterado para {} por {}", 
                    savedSupplier.getName(), newStatus, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Status do fornecedor atualizado com sucesso");
            response.put("supplier", responseDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao atualizar status do fornecedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    // ===== OPERAÇÕES DE CONSULTA =====

    @GetMapping("/categories")
    @Operation(summary = "Listar categorias disponíveis")
    public ResponseEntity<?> getAvailableCategories() {
        try {
            List<String> categories = supplierRepository.findDistinctCategoriesByStatus(SupplierStatus.ACTIVE);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            response.put("total", categories.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar categorias: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Listar fornecedores com melhor avaliação")
    public ResponseEntity<?> getTopRatedSuppliers(
            @Parameter(description = "Limite de resultados") @RequestParam(defaultValue = "10") int limit) {
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Supplier> topSuppliers = supplierRepository.findTopRatedSuppliers(SupplierStatus.ACTIVE, pageable);
            List<SupplierResponseDto> suppliers = dtoMapper.toSupplierDtoList(topSuppliers);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suppliers", suppliers);
            response.put("total", suppliers.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar top fornecedores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Obter estatísticas dos fornecedores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSupplierStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            statistics.put("totalActive", supplierRepository.countByStatus(SupplierStatus.ACTIVE));
            statistics.put("totalInactive", supplierRepository.countByStatus(SupplierStatus.INACTIVE));
            statistics.put("totalSuspended", supplierRepository.countByStatus(SupplierStatus.SUSPENDED));
            statistics.put("totalPending", supplierRepository.countByStatus(SupplierStatus.PENDING_VERIFICATION));
            
            List<String> categories = supplierRepository.findDistinctCategoriesByStatus(SupplierStatus.ACTIVE);
            statistics.put("totalCategories", categories.size());
            statistics.put("categories", categories);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao obter estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }
}
