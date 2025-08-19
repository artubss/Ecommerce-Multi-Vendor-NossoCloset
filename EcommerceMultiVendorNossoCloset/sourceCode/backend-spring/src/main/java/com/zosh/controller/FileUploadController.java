package com.zosh.controller;

import com.zosh.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para operações de upload de arquivos
 * Fase 1 - Semana 3: Sistema de Upload e Storage
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@Tag(name = "File Upload", description = "Operações de upload de arquivos (imagens e PDFs)")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    // ===== UPLOADS DE IMAGENS =====

    @PostMapping("/image/catalog-product")
    @Operation(summary = "Upload de imagem de produto de catálogo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<?> uploadCatalogProductImage(
            @Parameter(description = "Arquivo de imagem") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Nome do fornecedor") @RequestParam("supplierName") String supplierName,
            @Parameter(description = "Referência do produto") @RequestParam("productRef") String productRef) {
        
        try {
            String imageUrl = fileUploadService.uploadCatalogProductImage(file, supplierName, productRef);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagem do produto enviada com sucesso");
            response.put("imageUrl", imageUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            
            log.info("Upload de imagem de produto realizado: {} para fornecedor: {}", productRef, supplierName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro no upload de imagem de produto: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro no upload da imagem: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/image/custom-order")
    @Operation(summary = "Upload de imagem de pedido personalizado")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadCustomOrderImage(
            @Parameter(description = "Arquivo de imagem") @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID do usuário") @RequestParam("userId") Long userId,
            @Parameter(description = "ID do pedido") @RequestParam("orderId") Long orderId) {
        
        try {
            String imageUrl = fileUploadService.uploadCustomOrderImage(file, userId, orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagem do pedido enviada com sucesso");
            response.put("imageUrl", imageUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            
            log.info("Upload de imagem de pedido personalizado realizado: order-{} para user-{}", orderId, userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro no upload de imagem de pedido: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro no upload da imagem: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/image/user-profile")
    @Operation(summary = "Upload de imagem de perfil")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadUserProfileImage(
            @Parameter(description = "Arquivo de imagem") @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID do usuário") @RequestParam("userId") Long userId) {
        
        try {
            String imageUrl = fileUploadService.uploadUserProfileImage(file, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagem de perfil enviada com sucesso");
            response.put("imageUrl", imageUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            
            log.info("Upload de imagem de perfil realizado para user-{}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro no upload de imagem de perfil: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro no upload da imagem: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/image/catalog-bulk")
    @Operation(summary = "Upload de múltiplas imagens para catálogo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<?> uploadMultipleCatalogImages(
            @Parameter(description = "Arquivos de imagem") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Nome do fornecedor") @RequestParam("supplierName") String supplierName) {
        
        try {
            List<String> imageUrls = fileUploadService.uploadMultipleCatalogImages(files, supplierName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("%d imagens enviadas com sucesso", imageUrls.size()));
            response.put("imageUrls", imageUrls);
            response.put("totalFiles", files.size());
            response.put("successfulUploads", imageUrls.size());
            
            log.info("Upload em lote de {} imagens realizado para fornecedor: {}", imageUrls.size(), supplierName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro no upload em lote de imagens: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro no upload das imagens: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ===== UPLOADS DE PDFs =====

    @PostMapping("/pdf/catalog")
    @Operation(summary = "Upload de PDF de catálogo para Google Drive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<?> uploadCatalogPdf(
            @Parameter(description = "Arquivo PDF") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Nome do fornecedor") @RequestParam("supplierName") String supplierName,
            @Parameter(description = "Nome do catálogo") @RequestParam("catalogName") String catalogName) {
        
        try {
            String driveUrl = fileUploadService.uploadCatalogPdf(file, supplierName, catalogName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF do catálogo enviado com sucesso para o Google Drive");
            response.put("driveUrl", driveUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("supplierName", supplierName);
            response.put("catalogName", catalogName);
            
            log.info("Upload de PDF de catálogo realizado: {} para fornecedor: {}", catalogName, supplierName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro no upload de PDF de catálogo: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro no upload do PDF: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/pdf/admin-document")
    @Operation(summary = "Upload de documento administrativo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadAdminDocument(
            @Parameter(description = "Arquivo PDF") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Tipo do documento") @RequestParam("documentType") String documentType,
            @Parameter(description = "Descrição") @RequestParam(value = "description", required = false) String description) {
        
        try {
            String driveUrl = fileUploadService.uploadAdminDocument(file, documentType, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Documento administrativo enviado com sucesso");
            response.put("driveUrl", driveUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("documentType", documentType);
            
            log.info("Upload de documento administrativo realizado: {}", documentType);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro no upload de documento administrativo: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro no upload do documento: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ===== OPERAÇÕES DE VERIFICAÇÃO =====

    @GetMapping("/status")
    @Operation(summary = "Verifica status dos serviços de armazenamento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStorageStatus() {
        try {
            FileUploadService.StorageInfo storageInfo = fileUploadService.getStorageInfo();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("s3Available", storageInfo.isS3Available());
            response.put("googleDriveAvailable", storageInfo.isGoogleDriveAvailable());
            response.put("googleDriveQuota", storageInfo.getGoogleDriveQuota());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao verificar status do storage: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro ao verificar status: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/image/exists")
    @Operation(summary = "Verifica se imagem existe no S3")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<?> checkImageExists(@RequestParam("imageUrl") String imageUrl) {
        try {
            boolean exists = fileUploadService.imageExists(imageUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exists", exists);
            response.put("imageUrl", imageUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao verificar existência da imagem: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro na verificação: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ===== OPERAÇÕES DE DELEÇÃO =====

    @DeleteMapping("/image")
    @Operation(summary = "Deleta imagem do S3")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        try {
            fileUploadService.deleteImage(imageUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagem deletada com sucesso");
            response.put("imageUrl", imageUrl);
            
            log.info("Imagem deletada com sucesso: {}", imageUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao deletar imagem: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro ao deletar imagem: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/pdf")
    @Operation(summary = "Deleta PDF do Google Drive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePdf(@RequestParam("fileId") String fileId) {
        try {
            fileUploadService.deletePdf(fileId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF deletado com sucesso do Google Drive");
            response.put("fileId", fileId);
            
            log.info("PDF deletado com sucesso: {}", fileId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao deletar PDF: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro ao deletar PDF: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
