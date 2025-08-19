package com.zosh.domain;

/**
 * Níveis de urgência para pedidos personalizados
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum UrgencyLevel {
    LOW("Baixa - Sem pressa, prazo padrão"),
    NORMAL("Normal - Prazo padrão de entrega"),
    HIGH("Alta - Necessita priorização"),
    URGENT("Urgente - Máxima prioridade");

    private final String description;

    UrgencyLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
