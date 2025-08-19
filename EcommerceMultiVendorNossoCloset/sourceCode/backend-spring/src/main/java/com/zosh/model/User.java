package com.zosh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zosh.domain.DeliveryPreference;
import com.zosh.domain.USER_ROLE;
import com.zosh.domain.UserStatus;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidade User refatorada para o modelo de negócio Nosso Closet
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
@Entity
@Table(name = "app_users") // Renomeando para evitar conflito com palavra reservada 'user'
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String fullName;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "Celular é obrigatório")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Formato de celular inválido")
    private String mobile;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "WhatsApp é obrigatório")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Formato de WhatsApp inválido")
    private String whatsapp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private USER_ROLE role = USER_ROLE.ROLE_CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Saldo de crédito não pode ser negativo")
    private BigDecimal creditBalance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column
    private LocalDateTime lastAccess;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryPreference deliveryPreference = DeliveryPreference.NATAL_PICKUP;

    @Column(length = 200)
    @Size(max = 200, message = "Observações devem ter no máximo 200 caracteres")
    private String notes;

    @Column
    private LocalDateTime lastOrderDate;

    @Column
    private Integer totalOrders = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    // Relacionamentos refatorados
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomOrder> customOrders = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CreditTransaction> creditTransactions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private List<Address> addresses = new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "user_coupons",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "coupon_id")
    )
    private Set<Coupon> usedCoupons = new HashSet<>();

    // Campos de auditoria
    @Column(length = 50)
    private String createdBy;

    @Column(length = 50)
    private String updatedBy;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Métodos de conveniência
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return this.role == USER_ROLE.ROLE_ADMIN;
    }

    public boolean isCustomer() {
        return this.role == USER_ROLE.ROLE_CUSTOMER;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public void updateLastAccess() {
        this.lastAccess = LocalDateTime.now();
    }

    /**
     * Adiciona crédito ao saldo do usuário
     */
    public void addCredit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.creditBalance = this.creditBalance.add(amount);
        }
    }

    /**
     * Subtrai crédito do saldo do usuário
     */
    public void subtractCredit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newBalance = this.creditBalance.subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Saldo insuficiente");
            }
            this.creditBalance = newBalance;
        }
    }

    /**
     * Verifica se o usuário tem crédito suficiente
     */
    public boolean hasSufficientCredit(BigDecimal amount) {
        if (amount == null) {
            return true;
        }
        return this.creditBalance.compareTo(amount) >= 0;
    }

    /**
     * Atualiza estatísticas após um novo pedido
     */
    public void updateOrderStats(BigDecimal orderValue) {
        this.totalOrders = (this.totalOrders == null ? 0 : this.totalOrders) + 1;
        this.lastOrderDate = LocalDateTime.now();
        
        if (orderValue != null && orderValue.compareTo(BigDecimal.ZERO) > 0) {
            this.totalSpent = (this.totalSpent == null ? BigDecimal.ZERO : this.totalSpent).add(orderValue);
        }
    }

    /**
     * Verifica se é um cliente frequente (mais de 5 pedidos)
     */
    public boolean isFrequentCustomer() {
        return this.totalOrders != null && this.totalOrders >= 5;
    }

    /**
     * Verifica se é um cliente VIP (mais de R$ 1000 em compras)
     */
    public boolean isVipCustomer() {
        return this.totalSpent != null && this.totalSpent.compareTo(BigDecimal.valueOf(1000)) >= 0;
    }

    /**
     * Calcula há quantos dias o usuário não faz um pedido
     */
    public long getDaysSinceLastOrder() {
        if (lastOrderDate == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(lastOrderDate, LocalDateTime.now());
    }

    /**
     * Verifica se o usuário está inativo (sem pedidos por mais de 90 dias)
     */
    public boolean isInactiveCustomer() {
        return getDaysSinceLastOrder() > 90;
    }

    /**
     * Adiciona um endereço ao usuário
     */
    public void addAddress(Address address) {
        if (address != null) {
            this.addresses.add(address);
        }
    }

    /**
     * Remove um endereço do usuário
     */
    public void removeAddress(Address address) {
        this.addresses.remove(address);
    }

    /**
     * Obtém o endereço principal (primeiro da lista)
     */
    public Address getPrimaryAddress() {
        return addresses.isEmpty() ? null : addresses.get(0);
    }
}
