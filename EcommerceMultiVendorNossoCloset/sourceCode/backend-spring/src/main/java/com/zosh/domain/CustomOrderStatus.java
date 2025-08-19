package com.zosh.domain;

/**
 * Status dos pedidos personalizados
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum CustomOrderStatus {
    PENDING_ANALYSIS("Aguardando análise administrativa"),
    PRICED("Preço definido, aguardando confirmação do cliente"),
    CONFIRMED("Cliente confirmou interesse no preço"),
    IN_COLLECTIVE("Adicionado a um pedido coletivo"),
    SUPPLIER_PAID("Fornecedor foi pago pela empresa"),
    IN_TRANSIT("Produtos em trânsito do fornecedor"),
    RECEIVED("Produtos recebidos pela empresa"),
    DELIVERED("Entregue ao cliente final"),
    CANCELLED("Pedido cancelado pelo cliente ou admin"),
    REFUNDED("Reembolsado - valor convertido em créditos");

    private final String description;

    CustomOrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
