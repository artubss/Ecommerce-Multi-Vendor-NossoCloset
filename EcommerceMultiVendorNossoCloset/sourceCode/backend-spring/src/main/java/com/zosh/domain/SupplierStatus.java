package com.zosh.domain;

/**
 * Status dos fornecedores no sistema
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum SupplierStatus {
    ACTIVE("Ativo - Fornecedor operacional"),
    INACTIVE("Inativo - Fornecedor temporariamente desabilitado"),
    SUSPENDED("Suspenso - Fornecedor com problemas operacionais"),
    PENDING_VERIFICATION("Pendente - Aguardando verificação de dados");

    private final String description;

    SupplierStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
