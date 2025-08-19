package com.zosh.domain;

/**
 * Status dos créditos de clientes
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum CreditStatus {
    ACTIVE("Ativo - Disponível para uso"),
    USED("Usado - Crédito já utilizado"),
    EXPIRED("Expirado - Perdeu a validade"),
    BLOCKED("Bloqueado - Temporariamente indisponível"),
    PENDING("Pendente - Aguardando confirmação");

    private final String description;

    CreditStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
