package com.zosh.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Serviço Google Drive para upload e gerenciamento de PDFs
 * Fase 1 - Semana 3: Sistema de Upload e Storage
 */
@Slf4j
@Service
public class GoogleDriveService {

    @Autowired(required = false)
    private Drive driveService;

    @Value("${google.drive.folder.id:}")
    private String defaultFolderId;

    @Value("${google.drive.enabled:true}")
    private boolean googleDriveEnabled;

    @Autowired
    private BusinessMetricsService metricsService;

    /**
     * Faz upload de um arquivo PDF para o Google Drive
     */
    public String uploadPdf(MultipartFile file, String fileName, String description) throws IOException {
        if (!isGoogleDriveEnabled()) {
            log.warn("Google Drive não está configurado. Simulando upload de PDF: {}", fileName);
            return simulateUpload(fileName);
        }

        validatePdfFile(file);

        try {
            // Criar metadados do arquivo
            File fileMetadata = new File();
            fileMetadata.setName(generateFileName(fileName));
            fileMetadata.setDescription(description);
            
            // Se tiver pasta configurada, adicionar como pai
            if (defaultFolderId != null && !defaultFolderId.isEmpty()) {
                fileMetadata.setParents(Collections.singletonList(defaultFolderId));
            }

            // Criar conteúdo do arquivo
            InputStreamContent mediaContent = new InputStreamContent(
                "application/pdf",
                new ByteArrayInputStream(file.getBytes())
            );
            mediaContent.setLength(file.getSize());

            // Fazer upload
            File uploadedFile = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id,name,webViewLink,webContentLink")
                    .execute();

            // Tornar o arquivo público para visualização
            makeFilePublic(uploadedFile.getId());

            log.info("PDF enviado para Google Drive: {} (ID: {})", fileName, uploadedFile.getId());
            
            // Registrar métrica
            metricsService.recordPdfUpload();

            return uploadedFile.getWebViewLink();

        } catch (Exception e) {
            log.error("Erro ao enviar PDF para Google Drive: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no upload do PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Upload de PDF específico para catálogo
     */
    public String uploadCatalogPdf(MultipartFile file, String supplierName, String catalogName) throws IOException {
        String fileName = String.format("Catalogo_%s_%s_%s.pdf", 
            supplierName.replaceAll("[^a-zA-Z0-9]", "_"),
            catalogName.replaceAll("[^a-zA-Z0-9]", "_"),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        );
        
        String description = String.format("Catálogo de produtos - %s - %s", supplierName, catalogName);
        
        return uploadPdf(file, fileName, description);
    }

    /**
     * Lista arquivos de uma pasta específica
     */
    public List<File> listFiles(String folderId) throws IOException {
        if (!isGoogleDriveEnabled()) {
            log.warn("Google Drive não está configurado. Retornando lista vazia.");
            return Collections.emptyList();
        }

        try {
            String query = "'" + folderId + "' in parents and trashed=false";
            FileList result = driveService.files().list()
                    .setQ(query)
                    .setFields("files(id,name,webViewLink,createdTime,size)")
                    .execute();

            return result.getFiles();
        } catch (Exception e) {
            log.error("Erro ao listar arquivos do Google Drive: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao listar arquivos: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta um arquivo do Google Drive
     */
    public void deleteFile(String fileId) throws IOException {
        if (!isGoogleDriveEnabled()) {
            log.warn("Google Drive não está configurado. Simulando deleção de arquivo: {}", fileId);
            return;
        }

        try {
            driveService.files().delete(fileId).execute();
            log.info("Arquivo deletado do Google Drive: {}", fileId);
        } catch (Exception e) {
            log.error("Erro ao deletar arquivo do Google Drive: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao deletar arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtém informações de um arquivo
     */
    public File getFileInfo(String fileId) throws IOException {
        if (!isGoogleDriveEnabled()) {
            log.warn("Google Drive não está configurado. Retornando null para arquivo: {}", fileId);
            return null;
        }

        try {
            return driveService.files().get(fileId)
                    .setFields("id,name,webViewLink,webContentLink,createdTime,modifiedTime,size")
                    .execute();
        } catch (Exception e) {
            log.error("Erro ao obter informações do arquivo: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao obter informações do arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Cria uma pasta no Google Drive
     */
    public String createFolder(String folderName, String parentFolderId) throws IOException {
        if (!isGoogleDriveEnabled()) {
            log.warn("Google Drive não está configurado. Simulando criação de pasta: {}", folderName);
            return "simulated-folder-id";
        }

        try {
            File folderMetadata = new File();
            folderMetadata.setName(folderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            
            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                folderMetadata.setParents(Collections.singletonList(parentFolderId));
            }

            File folder = driveService.files()
                    .create(folderMetadata)
                    .setFields("id,name")
                    .execute();

            log.info("Pasta criada no Google Drive: {} (ID: {})", folderName, folder.getId());
            return folder.getId();

        } catch (Exception e) {
            log.error("Erro ao criar pasta no Google Drive: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao criar pasta: " + e.getMessage(), e);
        }
    }

    /**
     * Torna um arquivo público para visualização
     */
    private void makeFilePublic(String fileId) {
        try {
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");

            driveService.permissions()
                    .create(fileId, permission)
                    .execute();

            log.debug("Arquivo {} tornado público", fileId);
        } catch (Exception e) {
            log.warn("Erro ao tornar arquivo público: {}", e.getMessage());
            // Não falha a operação se não conseguir tornar público
        }
    }

    /**
     * Valida se o arquivo é um PDF válido
     */
    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Apenas arquivos PDF são permitidos");
        }

        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Tipo de arquivo inválido. Esperado: application/pdf");
        }

        // Validar tamanho (máximo 50MB)
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Arquivo muito grande. Tamanho máximo: 50MB");
        }

        // Validar tamanho mínimo (mínimo 1KB)
        if (file.getSize() < 1024) {
            throw new IllegalArgumentException("Arquivo muito pequeno. Tamanho mínimo: 1KB");
        }
    }

    /**
     * Gera um nome único para o arquivo
     */
    private String generateFileName(String originalName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        // Remove caracteres especiais do nome original
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        return String.format("%s_%s_%s", timestamp, uniqueId, safeName);
    }

    /**
     * Simula upload quando Google Drive não está configurado
     */
    private String simulateUpload(String fileName) {
        String simulatedUrl = String.format("https://drive.google.com/file/d/simulated_%s_%s/view", 
            UUID.randomUUID().toString().substring(0, 8),
            System.currentTimeMillis()
        );
        
        log.info("Upload simulado para arquivo: {} -> {}", fileName, simulatedUrl);
        return simulatedUrl;
    }

    /**
     * Verifica se o Google Drive está habilitado e configurado
     */
    public boolean isGoogleDriveEnabled() {
        return googleDriveEnabled && driveService != null;
    }

    /**
     * Obtém estatísticas de uso do Google Drive
     */
    public String getDriveQuotaInfo() {
        if (!isGoogleDriveEnabled()) {
            return "Google Drive não configurado";
        }

        try {
            // Aqui você pode implementar consulta de quota se necessário
            return "Google Drive configurado e operacional";
        } catch (Exception e) {
            return "Erro ao obter informações do Google Drive: " + e.getMessage();
        }
    }

    /**
     * Busca arquivos por nome
     */
    public List<File> searchFilesByName(String searchTerm) throws IOException {
        if (!isGoogleDriveEnabled()) {
            return Collections.emptyList();
        }

        try {
            String query = String.format("name contains '%s' and trashed=false", searchTerm);
            FileList result = driveService.files().list()
                    .setQ(query)
                    .setFields("files(id,name,webViewLink,createdTime)")
                    .execute();

            return result.getFiles();
        } catch (Exception e) {
            log.error("Erro ao buscar arquivos: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na busca: " + e.getMessage(), e);
        }
    }
}
