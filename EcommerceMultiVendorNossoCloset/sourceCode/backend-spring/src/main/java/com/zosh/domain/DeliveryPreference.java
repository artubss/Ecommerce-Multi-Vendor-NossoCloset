package com.zosh.domain;

/**
 * Preferências de entrega dos clientes
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum DeliveryPreference {
    NATAL_PICKUP("Retirada em Natal/RN"),
    HOME_DELIVERY("Entrega em domicílio"),
    PICKUP_POINT("Ponto de retirada específico"),
    FLEXIBLE("Flexível - Qualquer opção disponível");

    private final String description;

    DeliveryPreference(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
