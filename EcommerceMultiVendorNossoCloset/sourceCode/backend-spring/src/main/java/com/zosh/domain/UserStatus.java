package com.zosh.domain;

/**
 * Status dos usuários no sistema
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum UserStatus {
    ACTIVE("Ativo - Usuário operacional"),
    INACTIVE("Inativo - Conta temporariamente desabilitada"),
    SUSPENDED("Suspenso - Conta com restrições"),
    BLOCKED("Bloqueado - Acesso negado"),
    PENDING_VERIFICATION("Pendente - Aguardando verificação de conta");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
