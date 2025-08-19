package com.zosh.domain;

/**
 * Status dos pedidos coletivos
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum CollectiveOrderStatus {
    OPEN("Aberto para novos pedidos"),
    MINIMUM_REACHED("Mínimo atingido, fechando para novos pedidos"),
    PAYMENT_WINDOW("Janela de pagamento aberta para clientes"),
    SUPPLIER_PAID("Fornecedor foi pago pela empresa"),
    IN_TRANSIT("Produtos em trânsito do fornecedor"),
    RECEIVED("Produtos recebidos pela empresa"),
    DELIVERED("Todos os produtos entregues aos clientes"),
    CLOSED("Pedido coletivo fechado com sucesso"),
    CANCELLED("Pedido coletivo cancelado");

    private final String description;

    CollectiveOrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
