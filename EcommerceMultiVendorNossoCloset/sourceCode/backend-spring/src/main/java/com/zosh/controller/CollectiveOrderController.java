package com.zosh.controller;

import com.zosh.domain.CollectiveOrderStatus;
import com.zosh.dto.CollectiveOrderRequestDto;
import com.zosh.dto.CollectiveOrderResponseDto;
import com.zosh.exception.GlobalExceptionHandler;
import com.zosh.model.CollectiveOrder;
import com.zosh.model.Supplier;
import com.zosh.repository.CollectiveOrderRepository;
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
 * Controller REST para gerenciamento de pedidos coletivos
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Slf4j
@RestController
@RequestMapping("/api/collective-orders")
@Tag(name = "Collective Orders", description = "Operações de gerenciamento de pedidos coletivos")
public class CollectiveOrderController {

    @Autowired
    private CollectiveOrderRepository collectiveOrderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    // ===== OPERAÇÕES CRUD =====

    @PostMapping
    @Operation(summary = "Criar novo pedido coletivo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCollectiveOrder(
            @Valid @RequestBody CollectiveOrderRequestDto orderDto,
            Authentication authentication) {
        
        try {
            Optional<Supplier> supplier = supplierRepository.findById(orderDto.getSupplierId());
            if (supplier.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Fornecedor não encontrado");
            }

            CollectiveOrder order = new CollectiveOrder();
            order.setSupplier(supplier.get());
            order.setTitle(orderDto.getTitle());
            order.setDescription(orderDto.getDescription());
            order.setMinimumQuantity(orderDto.getMinimumQuantity());
            order.setMinimumOrderValue(orderDto.getMinimumOrderValue());
            order.setPaymentDeadline(orderDto.getPaymentDeadline());
            order.setNotes(orderDto.getNotes());
            order.setCreatedBy(authentication.getName());
            order.setUpdatedBy(authentication.getName());

            CollectiveOrder savedOrder = collectiveOrderRepository.save(order);
            CollectiveOrderResponseDto responseDto = toCollectiveOrderResponseDto(savedOrder);

            log.info("Pedido coletivo criado: {} por {}", savedOrder.getTitle(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido coletivo criado com sucesso");
            response.put("order", responseDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao criar pedido coletivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido coletivo por ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<?> getCollectiveOrderById(@PathVariable Long id) {
        try {
            Optional<CollectiveOrder> order = collectiveOrderRepository.findById(id);
            
            if (order.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Pedido coletivo não encontrado");
            }

            CollectiveOrderResponseDto responseDto = toCollectiveOrderResponseDto(order.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("order", responseDto);

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar pedido coletivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    // ===== OPERAÇÕES DE LISTAGEM =====

    @GetMapping
    @Operation(summary = "Listar pedidos coletivos com filtros")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCollectiveOrders(
            @Parameter(description = "Status") @RequestParam(required = false) CollectiveOrderStatus status,
            @Parameter(description = "ID do fornecedor") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "Apenas ativos") @RequestParam(defaultValue = "false") boolean activeOnly,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<CollectiveOrder> ordersPage;
            if (activeOnly) {
                ordersPage = collectiveOrderRepository.findActiveOrders(LocalDateTime.now(), pageable);
            } else {
                ordersPage = collectiveOrderRepository.findWithFilters(status, supplierId, pageable);
            }

            List<CollectiveOrderResponseDto> orders = ordersPage.getContent().stream()
                    .map(this::toCollectiveOrderResponseDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("totalElements", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar pedidos coletivos: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    @Operation(summary = "Listar pedidos coletivos ativos")
    public ResponseEntity<?> getActiveCollectiveOrders() {
        try {
            List<CollectiveOrder> activeOrders = collectiveOrderRepository.findActiveOrdersList(LocalDateTime.now());
            List<CollectiveOrderResponseDto> orders = activeOrders.stream()
                    .map(this::toCollectiveOrderResponseDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("total", orders.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar pedidos coletivos ativos: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Listar pedidos coletivos de um fornecedor")
    public ResponseEntity<?> getCollectiveOrdersBySupplier(@PathVariable Long supplierId) {
        try {
            Optional<Supplier> supplier = supplierRepository.findById(supplierId);
            if (supplier.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Fornecedor não encontrado");
            }

            List<CollectiveOrder> orders = collectiveOrderRepository.findBySupplierOrderByCreatedAtDesc(supplier.get());
            List<CollectiveOrderResponseDto> orderDtos = orders.stream()
                    .map(this::toCollectiveOrderResponseDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orderDtos);
            response.put("total", orderDtos.size());
            response.put("supplier", Map.of(
                "id", supplier.get().getId(),
                "name", supplier.get().getName()
            ));

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao listar pedidos do fornecedor: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    // ===== OPERAÇÕES DE FLUXO DE NEGÓCIO =====

    @PostMapping("/{id}/close")
    @Operation(summary = "Fechar pedido coletivo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> closeCollectiveOrder(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<CollectiveOrder> orderOpt = collectiveOrderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Pedido coletivo não encontrado");
            }

            CollectiveOrder order = orderOpt.get();
            if (order.getStatus() != CollectiveOrderStatus.ACTIVE) {
                throw new GlobalExceptionHandler.BusinessConflictException("Pedido deve estar ativo para ser fechado");
            }

            order.close();
            order.setUpdatedBy(authentication.getName());

            CollectiveOrder savedOrder = collectiveOrderRepository.save(order);
            CollectiveOrderResponseDto responseDto = toCollectiveOrderResponseDto(savedOrder);

            log.info("Pedido coletivo {} fechado por {}", order.getId(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido coletivo fechado com sucesso");
            response.put("order", responseDto);

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException | GlobalExceptionHandler.BusinessConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao fechar pedido coletivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Obter estatísticas dos pedidos coletivos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            statistics.put("totalActive", collectiveOrderRepository.countByStatus(CollectiveOrderStatus.ACTIVE));
            statistics.put("totalCompleted", collectiveOrderRepository.countByStatus(CollectiveOrderStatus.COMPLETED));
            statistics.put("totalCancelled", collectiveOrderRepository.countByStatus(CollectiveOrderStatus.CANCELLED));
            
            statistics.put("averageParticipants", collectiveOrderRepository.getAverageParticipants());
            statistics.put("totalValueProcessed", collectiveOrderRepository.getTotalValueProcessed());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao obter estatísticas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    // ===== MÉTODOS UTILITÁRIOS =====

    private CollectiveOrderResponseDto toCollectiveOrderResponseDto(CollectiveOrder order) {
        CollectiveOrderResponseDto dto = new CollectiveOrderResponseDto();
        dto.setId(order.getId());
        dto.setTitle(order.getTitle());
        dto.setDescription(order.getDescription());
        dto.setStatus(order.getStatus());
        dto.setMinimumQuantity(order.getMinimumQuantity());
        dto.setCurrentQuantity(order.getCurrentQuantity());
        dto.setMinimumOrderValue(order.getMinimumOrderValue());
        dto.setCurrentOrderValue(order.getCurrentOrderValue());
        dto.setPaymentDeadline(order.getPaymentDeadline());
        dto.setFulfillmentDate(order.getFulfillmentDate());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setUpdatedBy(order.getUpdatedBy());

        // Relacionamentos
        if (order.getSupplier() != null) {
            dto.setSupplier(new CollectiveOrderResponseDto.SupplierSummaryDto(
                order.getSupplier().getId(),
                order.getSupplier().getName(),
                order.getSupplier().getContactName(),
                order.getSupplier().getDeliveryTimeDays(),
                order.getSupplier().getAdminFeePercentage()
            ));
        }

        // Custom orders summary
        if (order.getCustomOrders() != null) {
            dto.setCustomOrders(order.getCustomOrders().stream()
                .map(co -> new CollectiveOrderResponseDto.CustomOrderSummaryDto(
                    co.getId(),
                    co.getClient().getFullName(),
                    co.getDescription(),
                    co.getFinalPrice(),
                    co.getQuantity(),
                    co.getCreatedAt()
                ))
                .collect(Collectors.toList()));
        }

        // Campos calculados
        dto.setProgressPercentage(order.getProgressPercentage());
        dto.setDaysUntilDeadline(order.getDaysUntilDeadline());
        dto.setActive(order.isActive());
        dto.setCompleted(order.isCompleted());
        dto.setExpired(order.isExpired());
        dto.setHasMinimumReached(order.hasMinimumReached());
        dto.setTotalParticipants(order.getTotalParticipants());

        return dto;
    }
}
