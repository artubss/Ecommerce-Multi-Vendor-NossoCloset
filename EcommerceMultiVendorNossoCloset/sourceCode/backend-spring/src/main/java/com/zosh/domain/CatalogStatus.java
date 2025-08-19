package com.zosh.domain;

/**
 * Status dos catálogos de fornecedores
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum CatalogStatus {
    ACTIVE("Ativo - Disponível para visualização"),
    INACTIVE("Inativo - Temporariamente indisponível"),
    EXPIRED("Expirado - Fora da validade"),
    PENDING_REVIEW("Pendente - Aguardando revisão administrativa");

    private final String description;

    CatalogStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
