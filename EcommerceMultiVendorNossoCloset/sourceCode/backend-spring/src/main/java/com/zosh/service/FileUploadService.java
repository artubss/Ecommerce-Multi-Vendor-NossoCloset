package com.zosh.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço unificado de upload que gerencia S3 (imagens) e Google Drive (PDFs)
 * Fase 1 - Semana 3: Sistema de Upload e Storage
 */
@Slf4j
@Service
public class FileUploadService {

    @Autowired
    private AwsS3Service awsS3Service;

    @Autowired
    private GoogleDriveService googleDriveService;

    @Autowired
    private BusinessMetricsService metricsService;

    // ===== UPLOADS DE IMAGENS (S3) =====

    /**
     * Upload de imagem para produto de catálogo
     */
    public String uploadCatalogProductImage(MultipartFile file, String supplierName, String productRef) throws IOException {
        validateImageFile(file);
        
        String category = String.format("catalog-products/%s", 
            supplierName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase());
        
        log.info("Fazendo upload de imagem de produto do catálogo: {} para fornecedor: {}", productRef, supplierName);
        
        String imageUrl = awsS3Service.uploadImage(file, category);
        metricsService.recordCatalogProductImageUpload();
        
        return imageUrl;
    }

    /**
     * Upload de imagem para pedido personalizado
     */
    public String uploadCustomOrderImage(MultipartFile file, Long userId, Long orderId) throws IOException {
        validateImageFile(file);
        
        String category = String.format("custom-orders/user-%d", userId);
        
        log.info("Fazendo upload de imagem de pedido personalizado: order-{} para user-{}", orderId, userId);
        
        String imageUrl = awsS3Service.uploadImage(file, category);
        metricsService.recordCustomOrderImageUpload();
        
        return imageUrl;
    }

    /**
     * Upload de imagem de perfil de usuário
     */
    public String uploadUserProfileImage(MultipartFile file, Long userId) throws IOException {
        validateImageFile(file);
        
        String category = "user-profiles";
        
        log.info("Fazendo upload de imagem de perfil para user-{}", userId);
        
        return awsS3Service.uploadImage(file, category);
    }

    /**
     * Upload de múltiplas imagens para catálogo
     */
    public List<String> uploadMultipleCatalogImages(List<MultipartFile> files, String supplierName) throws IOException {
        List<String> uploadedUrls = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file != null && !file.isEmpty()) {
                String productRef = String.format("bulk_upload_%d", i + 1);
                String url = uploadCatalogProductImage(file, supplierName, productRef);
                uploadedUrls.add(url);
            }
        }
        
        log.info("Upload de {} imagens concluído para fornecedor: {}", uploadedUrls.size(), supplierName);
        return uploadedUrls;
    }

    // ===== UPLOADS DE PDFs (GOOGLE DRIVE) =====

    /**
     * Upload de PDF de catálogo para Google Drive
     */
    public String uploadCatalogPdf(MultipartFile file, String supplierName, String catalogName) throws IOException {
        validatePdfFile(file);
        
        log.info("Fazendo upload de PDF de catálogo: {} para fornecedor: {}", catalogName, supplierName);
        
        String driveUrl = googleDriveService.uploadCatalogPdf(file, supplierName, catalogName);
        metricsService.recordCatalogPdfUpload();
        
        return driveUrl;
    }

    /**
     * Upload de documento administrativo
     */
    public String uploadAdminDocument(MultipartFile file, String documentType, String description) throws IOException {
        validatePdfFile(file);
        
        String fileName = String.format("%s_%s.pdf", 
            documentType.replaceAll("[^a-zA-Z0-9]", "_"),
            System.currentTimeMillis()
        );
        
        log.info("Fazendo upload de documento administrativo: {}", documentType);
        
        return googleDriveService.uploadPdf(file, fileName, description);
    }

    // ===== OPERAÇÕES DE DELEÇÃO =====

    /**
     * Deleta imagem do S3
     */
    public void deleteImage(String imageUrl) {
        try {
            awsS3Service.deleteImage(imageUrl);
            log.info("Imagem deletada: {}", imageUrl);
        } catch (Exception e) {
            log.error("Erro ao deletar imagem: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao deletar imagem: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta PDF do Google Drive
     */
    public void deletePdf(String driveFileId) {
        try {
            googleDriveService.deleteFile(driveFileId);
            log.info("PDF deletado do Google Drive: {}", driveFileId);
        } catch (Exception e) {
            log.error("Erro ao deletar PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao deletar PDF: " + e.getMessage(), e);
        }
    }

    // ===== OPERAÇÕES DE VERIFICAÇÃO =====

    /**
     * Verifica se imagem existe no S3
     */
    public boolean imageExists(String imageUrl) {
        return awsS3Service.imageExists(imageUrl);
    }

    /**
     * Verifica se o Google Drive está disponível
     */
    public boolean isGoogleDriveAvailable() {
        return googleDriveService.isGoogleDriveEnabled();
    }

    /**
     * Verifica se o S3 está disponível
     */
    public boolean isS3Available() {
        try {
            // Teste simples - verifica se consegue listar objetos
            return awsS3Service != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ===== OPERAÇÕES DE INFORMAÇÃO =====

    /**
     * Obtém informações sobre o armazenamento
     */
    public StorageInfo getStorageInfo() {
        return StorageInfo.builder()
            .s3Available(isS3Available())
            .googleDriveAvailable(isGoogleDriveAvailable())
            .googleDriveQuota(googleDriveService.getDriveQuotaInfo())
            .build();
    }

    // ===== VALIDAÇÕES PRIVADAS =====

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de imagem não pode ser vazio");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Arquivo deve ser uma imagem válida (JPEG, PNG, etc.)");
        }

        // Validar extensão
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".jpg") && 
                                !filename.toLowerCase().endsWith(".jpeg") && 
                                !filename.toLowerCase().endsWith(".png") && 
                                !filename.toLowerCase().endsWith(".webp"))) {
            throw new IllegalArgumentException("Formato de imagem não suportado. Use: JPG, JPEG, PNG ou WEBP");
        }

        // Validar tamanho (máximo 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Imagem muito grande. Tamanho máximo: 10MB");
        }

        // Validar tamanho mínimo (1KB)
        if (file.getSize() < 1024) {
            throw new IllegalArgumentException("Imagem muito pequena. Tamanho mínimo: 1KB");
        }
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo PDF não pode ser vazio");
        }

        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Arquivo deve ser um PDF válido");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Arquivo deve ter extensão .pdf");
        }

        // Validar tamanho (máximo 50MB para PDFs)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("PDF muito grande. Tamanho máximo: 50MB");
        }

        // Validar tamanho mínimo (1KB)
        if (file.getSize() < 1024) {
            throw new IllegalArgumentException("PDF muito pequeno. Tamanho mínimo: 1KB");
        }
    }

    // ===== CLASSE DE INFORMAÇÕES =====

    public static class StorageInfo {
        private boolean s3Available;
        private boolean googleDriveAvailable;
        private String googleDriveQuota;

        public static StorageInfoBuilder builder() {
            return new StorageInfoBuilder();
        }

        public static class StorageInfoBuilder {
            private boolean s3Available;
            private boolean googleDriveAvailable;
            private String googleDriveQuota;

            public StorageInfoBuilder s3Available(boolean s3Available) {
                this.s3Available = s3Available;
                return this;
            }

            public StorageInfoBuilder googleDriveAvailable(boolean googleDriveAvailable) {
                this.googleDriveAvailable = googleDriveAvailable;
                return this;
            }

            public StorageInfoBuilder googleDriveQuota(String googleDriveQuota) {
                this.googleDriveQuota = googleDriveQuota;
                return this;
            }

            public StorageInfo build() {
                StorageInfo info = new StorageInfo();
                info.s3Available = this.s3Available;
                info.googleDriveAvailable = this.googleDriveAvailable;
                info.googleDriveQuota = this.googleDriveQuota;
                return info;
            }
        }

        // Getters
        public boolean isS3Available() { return s3Available; }
        public boolean isGoogleDriveAvailable() { return googleDriveAvailable; }
        public String getGoogleDriveQuota() { return googleDriveQuota; }
    }
}
