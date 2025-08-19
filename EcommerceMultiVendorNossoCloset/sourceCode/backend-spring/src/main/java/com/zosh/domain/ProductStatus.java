package com.zosh.domain;

/**
 * Status dos produtos nos catálogos
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum ProductStatus {
    AVAILABLE("Disponível - Produto pode ser pedido"),
    OUT_OF_STOCK("Sem estoque - Temporariamente indisponível no fornecedor"),
    DISCONTINUED("Descontinuado - Não será mais fornecido"),
    PENDING_APPROVAL("Pendente - Aguardando aprovação administrativa");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
