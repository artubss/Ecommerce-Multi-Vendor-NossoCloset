package com.zosh.controller;

import com.zosh.domain.CustomOrderStatus;
import com.zosh.domain.UrgencyLevel;
import com.zosh.dto.CustomOrderRequestDto;
import com.zosh.dto.CustomOrderResponseDto;
import com.zosh.model.CustomOrder;
import com.zosh.model.Supplier;
import com.zosh.model.User;
import com.zosh.repository.CustomOrderRepository;
import com.zosh.repository.SupplierRepository;
import com.zosh.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller REST para gerenciamento de pedidos personalizados
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Slf4j
@RestController
@RequestMapping("/api/custom-orders")
@Tag(name = "Custom Orders", description = "Operações de gerenciamento de pedidos personalizados")
public class CustomOrderController {

    @Autowired
    private CustomOrderRepository customOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private DtoMapperService dtoMapper;

    // ===== OPERAÇÕES CRUD =====

    @PostMapping
    @Operation(summary = "Criar novo pedido personalizado")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> createCustomOrder(
            @Valid @RequestBody CustomOrderRequestDto orderDto,
            Authentication authentication) {
        
        try {
            // Buscar cliente
            Optional<User> client = userRepository.findById(orderDto.getClientId());
            if (client.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Cliente não encontrado")
                );
            }

            // Verificar se o usuário pode criar pedido para este cliente
            if (!authentication.getName().equals(client.get().getEmail()) && 
                !authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("success", false, "message", "Acesso negado")
                );
            }

            CustomOrder order = dtoMapper.toCustomOrderEntity(orderDto, client.get());
            order.setCreatedBy(authentication.getName());
            order.setUpdatedBy(authentication.getName());

            CustomOrder savedOrder = customOrderRepository.save(order);
            CustomOrderResponseDto responseDto = dtoMapper.toCustomOrderDto(savedOrder);

            // Atualizar estatísticas do cliente
            client.get().updateOrderStats(savedOrder.getTotalValue());
            userRepository.save(client.get());

            log.info("Pedido personalizado criado com sucesso: {} por {}", savedOrder.getId(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido personalizado criado com sucesso");
            response.put("order", responseDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Erro ao criar pedido personalizado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido personalizado por ID")
    @PreAuthorize("hasRole('ADMIN') or @customOrderController.isOwnerOrAdmin(#id, authentication)")
    public ResponseEntity<?> getCustomOrderById(@PathVariable Long id) {
        try {
            Optional<CustomOrder> order = customOrderRepository.findById(id);
            
            if (order.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Pedido não encontrado")
                );
            }

            CustomOrderResponseDto responseDto = dtoMapper.toCustomOrderDto(order.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("order", responseDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao buscar pedido personalizado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pedido personalizado")
    @PreAuthorize("hasRole('ADMIN') or @customOrderController.isOwnerOrAdmin(#id, authentication)")
    public ResponseEntity<?> updateCustomOrder(
            @PathVariable Long id,
            @Valid @RequestBody CustomOrderRequestDto orderDto,
            Authentication authentication) {
        
        try {
            Optional<CustomOrder> existingOrder = customOrderRepository.findById(id);
            
            if (existingOrder.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Pedido não encontrado")
                );
            }

            CustomOrder order = existingOrder.get();

            // Verificar se pedido pode ser atualizado
            if (order.getStatus() != CustomOrderStatus.PENDING_ANALYSIS && 
                !authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Pedido não pode ser alterado neste status")
                );
            }

            dtoMapper.updateCustomOrderFromDto(order, orderDto);
            order.setUpdatedBy(authentication.getName());

            CustomOrder savedOrder = customOrderRepository.save(order);
            CustomOrderResponseDto responseDto = dtoMapper.toCustomOrderDto(savedOrder);

            log.info("Pedido personalizado atualizado com sucesso: {} por {}", savedOrder.getId(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido atualizado com sucesso");
            response.put("order", responseDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao atualizar pedido personalizado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    // ===== OPERAÇÕES DE FLUXO DE NEGÓCIO =====

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Analisar e precificar pedido")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> analyzeOrder(
            @PathVariable Long id,
            @Parameter(description = "ID do fornecedor") @RequestParam Long supplierId,
            @Parameter(description = "Preço final") @RequestParam BigDecimal finalPrice,
            @Parameter(description = "Notas administrativas") @RequestParam(required = false) String adminNotes,
            Authentication authentication) {
        
        try {
            Optional<CustomOrder> orderOpt = customOrderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Pedido não encontrado")
                );
            }

            Optional<Supplier> supplierOpt = supplierRepository.findById(supplierId);
            if (supplierOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Fornecedor não encontrado")
                );
            }

            CustomOrder order = orderOpt.get();
            if (order.getStatus() != CustomOrderStatus.PENDING_ANALYSIS) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Pedido não está pendente de análise")
                );
            }

            Supplier supplier = supplierOpt.get();
            order.analyze(authentication.getName(), finalPrice, supplier);
            if (adminNotes != null) {
                order.setAdminNotes(adminNotes);
            }
            order.setUpdatedBy(authentication.getName());

            CustomOrder savedOrder = customOrderRepository.save(order);
            CustomOrderResponseDto responseDto = dtoMapper.toCustomOrderDto(savedOrder);

            log.info("Pedido {} analisado e precificado por {}: R$ {}", 
                    order.getId(), authentication.getName(), finalPrice);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido analisado e precificado com sucesso");
            response.put("order", responseDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao analisar pedido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirmar pedido pelo cliente")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<CustomOrder> orderOpt = customOrderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Pedido não encontrado")
                );
            }

            CustomOrder order = orderOpt.get();

            // Verificar se é o dono do pedido ou admin
            if (!authentication.getName().equals(order.getClient().getEmail()) && 
                !authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("success", false, "message", "Acesso negado")
                );
            }

            if (order.getStatus() != CustomOrderStatus.PRICED) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Pedido deve estar precificado para ser confirmado")
                );
            }

            order.confirm();
            order.setUpdatedBy(authentication.getName());

            CustomOrder savedOrder = customOrderRepository.save(order);
            CustomOrderResponseDto responseDto = dtoMapper.toCustomOrderDto(savedOrder);

            log.info("Pedido {} confirmado pelo cliente: {}", order.getId(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido confirmado com sucesso");
            response.put("order", responseDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao confirmar pedido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pedido")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @Parameter(description = "Motivo do cancelamento") @RequestParam String reason,
            Authentication authentication) {
        
        try {
            Optional<CustomOrder> orderOpt = customOrderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Pedido não encontrado")
                );
            }

            CustomOrder order = orderOpt.get();

            // Verificar se é o dono do pedido ou admin
            if (!authentication.getName().equals(order.getClient().getEmail()) && 
                !authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("success", false, "message", "Acesso negado")
                );
            }

            // Verificar se pedido pode ser cancelado
            if (order.getStatus() == CustomOrderStatus.DELIVERED || 
                order.getStatus() == CustomOrderStatus.CANCELLED) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Pedido não pode ser cancelado neste status")
                );
            }

            order.cancel(reason);
            order.setUpdatedBy(authentication.getName());

            CustomOrder savedOrder = customOrderRepository.save(order);
            CustomOrderResponseDto responseDto = dtoMapper.toCustomOrderDto(savedOrder);

            log.info("Pedido {} cancelado por {}: {}", order.getId(), authentication.getName(), reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido cancelado com sucesso");
            response.put("order", responseDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao cancelar pedido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    // ===== OPERAÇÕES DE LISTAGEM =====

    @GetMapping
    @Operation(summary = "Listar pedidos personalizados com filtros")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCustomOrders(
            @Parameter(description = "Status do pedido") @RequestParam(required = false) CustomOrderStatus status,
            @Parameter(description = "ID do fornecedor") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "Urgência") @RequestParam(required = false) UrgencyLevel urgency,
            @Parameter(description = "Categoria") @RequestParam(required = false) String category,
            @Parameter(description = "Valor mínimo") @RequestParam(required = false) BigDecimal minValue,
            @Parameter(description = "Valor máximo") @RequestParam(required = false) BigDecimal maxValue,
            @Parameter(description = "Data inicial") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "Data final") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Termo de busca") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<CustomOrder> ordersPage = customOrderRepository.findWithFilters(
                status, supplierId, urgency, category, minValue, maxValue, 
                startDate, endDate, searchTerm, pageable
            );

            List<CustomOrderResponseDto> orders = dtoMapper.toCustomOrderDtoList(ordersPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("totalElements", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("hasNext", ordersPage.hasNext());
            response.put("hasPrevious", ordersPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar pedidos personalizados: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Listar pedidos do cliente atual")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getMyOrders(
            Authentication authentication,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Optional<User> user = userRepository.findByEmail(authentication.getName());
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Usuário não encontrado")
                );
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<CustomOrder> ordersPage = customOrderRepository.findByClient(user.get(), pageable);

            List<CustomOrderResponseDto> orders = dtoMapper.toCustomOrderDtoList(ordersPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("totalElements", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar pedidos do cliente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @GetMapping("/pending-analysis")
    @Operation(summary = "Listar pedidos pendentes de análise")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingAnalysisOrders() {
        try {
            List<CustomOrder> pendingOrders = customOrderRepository.findPendingAnalysis();
            List<CustomOrderResponseDto> orders = dtoMapper.toCustomOrderDtoList(pendingOrders);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("total", orders.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar pedidos pendentes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    @GetMapping("/urgent")
    @Operation(summary = "Listar pedidos urgentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUrgentOrders() {
        try {
            List<UrgencyLevel> urgentLevels = List.of(UrgencyLevel.HIGH, UrgencyLevel.URGENT);
            List<CustomOrder> urgentOrders = customOrderRepository.findByUrgencyInOrderByCreatedAtAsc(urgentLevels);
            List<CustomOrderResponseDto> orders = dtoMapper.toCustomOrderDtoList(urgentOrders);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("total", orders.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar pedidos urgentes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Erro interno do servidor: " + e.getMessage())
            );
        }
    }

    // ===== MÉTODOS DE UTILITÁRIOS =====

    /**
     * Verifica se o usuário é dono do pedido ou admin
     */
    public boolean isOwnerOrAdmin(Long orderId, Authentication authentication) {
        try {
            Optional<CustomOrder> order = customOrderRepository.findById(orderId);
            if (order.isEmpty()) {
                return false;
            }

            // Se for admin, tem acesso
            if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }

            // Se for o dono do pedido, tem acesso
            return authentication.getName().equals(order.get().getClient().getEmail());
            
        } catch (Exception e) {
            log.error("Erro ao verificar ownership: {}", e.getMessage());
            return false;
        }
    }
}
