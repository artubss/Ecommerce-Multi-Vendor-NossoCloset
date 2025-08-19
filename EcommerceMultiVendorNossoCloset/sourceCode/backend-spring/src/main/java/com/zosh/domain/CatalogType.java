package com.zosh.domain;

/**
 * Tipos de catálogo de fornecedores
 * Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
 */
public enum CatalogType {
    PDF("Catálogo em formato PDF"),
    IMAGE_COLLECTION("Coleção de imagens organizadas"),
    ONLINE_LINK("Link para catálogo online do fornecedor"),
    MIXED("Catálogo misto com PDFs e imagens");

    private final String description;

    CatalogType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
