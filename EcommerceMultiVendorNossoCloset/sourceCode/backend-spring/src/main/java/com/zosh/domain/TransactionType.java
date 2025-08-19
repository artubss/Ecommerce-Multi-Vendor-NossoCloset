package com.zosh.domain;

/**
 * Tipos de transação de crédito
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum TransactionType {
    REFUND("Reembolso por cancelamento ou problema"),
    CREDIT_BONUS("Bônus por aceitar crédito em vez de reembolso"),
    REFERRAL_BONUS("Bônus por indicação de novo cliente"),
    LOYALTY_BONUS("Bônus de fidelidade por compras frequentes"),
    PROMOTIONAL_CREDIT("Crédito promocional ou campanha"),
    TRANSFER("Transferência entre clientes"),
    EXPIRATION_DEBIT("Débito por expiração de crédito"),
    USAGE_DEBIT("Débito por uso de crédito em pedido");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
