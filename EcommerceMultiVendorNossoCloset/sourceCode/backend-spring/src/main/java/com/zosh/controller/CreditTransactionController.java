package com.zosh.controller;

import com.zosh.domain.CreditStatus;
import com.zosh.domain.TransactionType;
import com.zosh.dto.CreditTransactionRequestDto;
import com.zosh.dto.CreditTransactionResponseDto;
import com.zosh.exception.GlobalExceptionHandler;
import com.zosh.model.CreditTransaction;
import com.zosh.model.User;
import com.zosh.repository.CreditTransactionRepository;
import com.zosh.repository.UserRepository;
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
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de transações de crédito
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Slf4j
@RestController
@RequestMapping("/api/credit-transactions")
@Tag(name = "Credit Transactions", description = "Operações de gerenciamento de créditos")
public class CreditTransactionController {

    @Autowired
    private CreditTransactionRepository creditTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    // ===== OPERAÇÕES CRUD =====

    @PostMapping
    @Operation(summary = "Criar nova transação de crédito")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCreditTransaction(
            @Valid @RequestBody CreditTransactionRequestDto transactionDto,
            Authentication authentication) {
        
        try {
            Optional<User> user = userRepository.findById(transactionDto.getUserId());
            if (user.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Usuário não encontrado");
            }

            User targetUser = user.get();

            // Verificar se há saldo suficiente para débito
            if (transactionDto.getType() == TransactionType.DEBIT && 
                !targetUser.hasSufficientCredit(transactionDto.getAmount())) {
                throw new GlobalExceptionHandler.BusinessConflictException("Saldo insuficiente para débito");
            }

            CreditTransaction transaction = new CreditTransaction();
            transaction.setUser(targetUser);
            transaction.setAmount(transactionDto.getAmount());
            transaction.setType(transactionDto.getType());
            transaction.setDescription(transactionDto.getDescription());
            transaction.setNotes(transactionDto.getNotes());
            transaction.setCreatedBy(authentication.getName());
            transaction.setUpdatedBy(authentication.getName());

            // Processar transação imediatamente
            transaction.process(authentication.getName());

            // Atualizar saldo do usuário
            if (transactionDto.getType() == TransactionType.CREDIT) {
                targetUser.addCredit(transactionDto.getAmount());
            } else {
                targetUser.subtractCredit(transactionDto.getAmount());
            }

            CreditTransaction savedTransaction = creditTransactionRepository.save(transaction);
            userRepository.save(targetUser);

            CreditTransactionResponseDto responseDto = toCreditTransactionResponseDto(savedTransaction);

            log.info("Transação de crédito criada: {} {} para usuário {} por {}", 
                    transactionDto.getType(), transactionDto.getAmount(), 
                    targetUser.getEmail(), authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transação processada com sucesso");
            response.put("transaction", responseDto);
            response.put("newBalance", targetUser.getCreditBalance());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException | GlobalExceptionHandler.BusinessConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao criar transação de crédito: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID")
    @PreAuthorize("hasRole('ADMIN') or @creditTransactionController.isOwnerOrAdmin(#id, authentication)")
    public ResponseEntity<?> getCreditTransactionById(@PathVariable Long id) {
        try {
            Optional<CreditTransaction> transaction = creditTransactionRepository.findById(id);
            
            if (transaction.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Transação não encontrada");
            }

            CreditTransactionResponseDto responseDto = toCreditTransactionResponseDto(transaction.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transaction", responseDto);

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar transação: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    // ===== OPERAÇÕES DE LISTAGEM =====

    @GetMapping
    @Operation(summary = "Listar transações com filtros")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCreditTransactions(
            @Parameter(description = "ID do usuário") @RequestParam(required = false) Long userId,
            @Parameter(description = "Tipo de transação") @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Status") @RequestParam(required = false) CreditStatus status,
            @Parameter(description = "Data inicial") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "Data final") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Ordenação") @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<CreditTransaction> transactionsPage = creditTransactionRepository.findWithFilters(
                userId, type, status, startDate, endDate, pageable
            );

            List<CreditTransactionResponseDto> transactions = transactionsPage.getContent().stream()
                    .map(this::toCreditTransactionResponseDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions);
            response.put("totalElements", transactionsPage.getTotalElements());
            response.put("totalPages", transactionsPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar transações: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/my-transactions")
    @Operation(summary = "Listar transações do usuário atual")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getMyTransactions(
            Authentication authentication,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Optional<User> user = userRepository.findByEmail(authentication.getName());
            if (user.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Usuário não encontrado");
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
            Page<CreditTransaction> transactionsPage = creditTransactionRepository.findByUser(user.get(), pageable);

            List<CreditTransactionResponseDto> transactions = transactionsPage.getContent().stream()
                    .map(this::toCreditTransactionResponseDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions);
            response.put("totalElements", transactionsPage.getTotalElements());
            response.put("totalPages", transactionsPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("currentBalance", user.get().getCreditBalance());

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao listar transações do usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/balance/{userId}")
    @Operation(summary = "Obter saldo atual do usuário")
    @PreAuthorize("hasRole('ADMIN') or @creditTransactionController.isUserOrAdmin(#userId, authentication)")
    public ResponseEntity<?> getUserBalance(@PathVariable Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                throw new GlobalExceptionHandler.ResourceNotFoundException("Usuário não encontrado");
            }

            User targetUser = user.get();
            BigDecimal balance = targetUser.getCreditBalance();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("balance", balance);
            response.put("formattedBalance", formatCurrency(balance));
            response.put("lastTransaction", creditTransactionRepository.findTopByUserOrderByTransactionDateDesc(targetUser)
                    .map(this::toCreditTransactionResponseDto)
                    .orElse(null));

            return ResponseEntity.ok(response);

        } catch (GlobalExceptionHandler.ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao obter saldo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Obter estatísticas das transações")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            statistics.put("totalCredits", creditTransactionRepository.getTotalByType(TransactionType.CREDIT));
            statistics.put("totalDebits", creditTransactionRepository.getTotalByType(TransactionType.DEBIT));
            statistics.put("totalTransactions", creditTransactionRepository.count());
            statistics.put("totalUsersWithBalance", userRepository.countUsersWithBalance());
            statistics.put("averageBalance", userRepository.getAverageBalance());

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

    private CreditTransactionResponseDto toCreditTransactionResponseDto(CreditTransaction transaction) {
        CreditTransactionResponseDto dto = new CreditTransactionResponseDto();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setDescription(transaction.getDescription());
        dto.setNotes(transaction.getNotes());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setProcessedAt(transaction.getProcessedAt());
        dto.setProcessedBy(transaction.getProcessedBy());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        dto.setCreatedBy(transaction.getCreatedBy());
        dto.setUpdatedBy(transaction.getUpdatedBy());

        // User summary
        if (transaction.getUser() != null) {
            dto.setUser(new CreditTransactionResponseDto.UserSummaryDto(
                transaction.getUser().getId(),
                transaction.getUser().getFullName(),
                transaction.getUser().getEmail(),
                transaction.getUser().getCreditBalance()
            ));
        }

        // Related order summary
        if (transaction.getRelatedOrder() != null) {
            dto.setRelatedOrder(new CreditTransactionResponseDto.OrderSummaryDto(
                transaction.getRelatedOrder().getId(),
                transaction.getRelatedOrder().getDescription(),
                transaction.getRelatedOrder().getTotalValue(),
                transaction.getRelatedOrder().getStatus().name()
            ));
        }

        // Campos calculados
        dto.setCredit(transaction.isCredit());
        dto.setDebit(transaction.isDebit());
        dto.setPending(transaction.isPending());
        dto.setCompleted(transaction.isCompleted());
        dto.setFormattedAmount(formatCurrency(transaction.getAmount()));

        return dto;
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return formatter.format(amount);
    }

    /**
     * Verifica se o usuário é dono da transação ou admin
     */
    public boolean isOwnerOrAdmin(Long transactionId, Authentication authentication) {
        try {
            Optional<CreditTransaction> transaction = creditTransactionRepository.findById(transactionId);
            if (transaction.isEmpty()) {
                return false;
            }

            if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }

            return authentication.getName().equals(transaction.get().getUser().getEmail());
            
        } catch (Exception e) {
            log.error("Erro ao verificar ownership: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se é o próprio usuário ou admin
     */
    public boolean isUserOrAdmin(Long userId, Authentication authentication) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return false;
            }

            if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }

            return authentication.getName().equals(user.get().getEmail());
            
        } catch (Exception e) {
            log.error("Erro ao verificar user access: {}", e.getMessage());
            return false;
        }
    }
}
