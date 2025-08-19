package com.zosh.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Serviço AWS S3 para upload e gerenciamento de imagens
 * Fase 1 - Semana 3: Sistema de Upload e Storage
 */
@Slf4j
@Service
public class AwsS3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private static final String IMAGE_FOLDER = "images";
    private static final String THUMBNAIL_FOLDER = "thumbnails";
    private static final int THUMBNAIL_WIDTH = 300;
    private static final int THUMBNAIL_HEIGHT = 300;
    private static final double COMPRESSION_QUALITY = 0.8;

    /**
     * Upload de imagem com compressão automática
     */
    public String uploadImage(MultipartFile file, String category) throws IOException {
        try {
            // Validar arquivo
            validateImageFile(file);

            // Gerar nome único para o arquivo
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String key = buildImageKey(category, fileName);

            // Comprimir imagem
            byte[] compressedImage = compressImage(file.getInputStream());

            // Upload para S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(compressedImage.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(compressedImage));

            // Criar thumbnail
            createThumbnail(compressedImage, category, fileName);

            String imageUrl = buildImageUrl(key);
            log.info("Imagem enviada com sucesso: {}", imageUrl);

            return imageUrl;

        } catch (Exception e) {
            log.error("Erro ao fazer upload da imagem: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no upload da imagem", e);
        }
    }

    /**
     * Upload de imagem sem compressão (para casos especiais)
     */
    public String uploadImageOriginal(MultipartFile file, String category) throws IOException {
        try {
            validateImageFile(file);

            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String key = buildImageKey(category, fileName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String imageUrl = buildImageUrl(key);
            log.info("Imagem original enviada com sucesso: {}", imageUrl);

            return imageUrl;

        } catch (Exception e) {
            log.error("Erro ao fazer upload da imagem original: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no upload da imagem original", e);
        }
    }

    /**
     * Deletar imagem do S3
     */
    public void deleteImage(String imageUrl) {
        try {
            String key = extractKeyFromUrl(imageUrl);
            
            // Deletar imagem principal
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);

            // Deletar thumbnail
            String thumbnailKey = buildThumbnailKey(key);
            DeleteObjectRequest thumbnailDeleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(thumbnailKey)
                    .build();
            s3Client.deleteObject(thumbnailDeleteRequest);

            log.info("Imagem deletada com sucesso: {}", key);

        } catch (Exception e) {
            log.error("Erro ao deletar imagem: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao deletar imagem", e);
        }
    }

    /**
     * Verificar se arquivo existe no S3
     */
    public boolean imageExists(String imageUrl) {
        try {
            String key = extractKeyFromUrl(imageUrl);
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Erro ao verificar existência da imagem: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obter URL pública da imagem
     */
    public String getPublicUrl(String imageUrl) {
        return buildImageUrl(extractKeyFromUrl(imageUrl));
    }

    // ===== MÉTODOS PRIVADOS =====

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser nulo ou vazio");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("Arquivo muito grande. Tamanho máximo: 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Arquivo deve ser uma imagem válida");
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return timestamp + "_" + uuid + extension;
    }

    private String buildImageKey(String category, String fileName) {
        return String.format("%s/%s/%s", IMAGE_FOLDER, category, fileName);
    }

    private String buildThumbnailKey(String imageKey) {
        return imageKey.replace(IMAGE_FOLDER, THUMBNAIL_FOLDER);
    }

    private String buildImageUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    private String extractKeyFromUrl(String imageUrl) {
        // Remove a parte base da URL para obter apenas a chave
        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        return imageUrl.replace(baseUrl, "");
    }

    private byte[] compressImage(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Thumbnails.of(inputStream)
                .size(1920, 1080) // Máximo 1920x1080
                .outputQuality(COMPRESSION_QUALITY)
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    private void createThumbnail(byte[] imageData, String category, String fileName) throws IOException {
        try {
            String thumbnailKey = buildThumbnailKey(buildImageKey(category, fileName));
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            Thumbnails.of(inputStream)
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .keepAspectRatio(true)
                    .outputQuality(0.7)
                    .toOutputStream(outputStream);

            byte[] thumbnailData = outputStream.toByteArray();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(thumbnailKey)
                    .contentType("image/jpeg")
                    .contentLength(thumbnailData.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(thumbnailData));

        } catch (Exception e) {
            log.warn("Erro ao criar thumbnail: {}", e.getMessage());
            // Não falha o upload principal se o thumbnail falhar
        }
    }
}
