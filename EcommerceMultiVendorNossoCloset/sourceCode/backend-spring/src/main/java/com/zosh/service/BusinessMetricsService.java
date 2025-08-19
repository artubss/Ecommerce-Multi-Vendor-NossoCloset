package com.zosh.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Serviço de métricas customizadas para monitoramento do negócio
 * Fase 1 - Semana 1: Setup de Infraestrutura
 */
@Slf4j
@Service
public class BusinessMetricsService {

    @Autowired
    private MeterRegistry meterRegistry;

    // ===== MÉTRICAS DE PEDIDOS =====

    /**
     * Registrar criação de pedido personalizado
     */
    public void recordCustomOrderCreated() {
        Counter.builder("custom_orders.created")
                .description("Total de pedidos personalizados criados")
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: custom_orders.created");
    }

    /**
     * Registrar pedido coletivo fechado
     */
    public void recordCollectiveOrderClosed() {
        Counter.builder("collective_orders.closed")
                .description("Total de pedidos coletivos fechados")
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: collective_orders.closed");
    }

    /**
     * Registrar tempo de processamento de pedido
     */
    public void recordOrderProcessingTime(Duration processingTime) {
        Timer.builder("order.processing.duration")
                .description("Tempo de processamento de pedidos")
                .register(meterRegistry)
                .record(processingTime);
        log.debug("Métrica registrada: order.processing.duration = {}ms", processingTime.toMillis());
    }

    // ===== MÉTRICAS DE FORNECEDORES =====

    /**
     * Registrar pagamento a fornecedor
     */
    public void recordSupplierPayment(String supplierName, double amount) {
        Counter.builder("supplier.payment.total")
                .description("Total pago a fornecedores")
                .tag("supplier", supplierName)
                .register(meterRegistry)
                .increment(amount);
        log.debug("Métrica registrada: supplier.payment.total para {} = {}", supplierName, amount);
    }

    /**
     * Registrar tempo de resposta do fornecedor
     */
    public void recordSupplierResponseTime(String supplierName, Duration responseTime) {
        Timer.builder("supplier.response.duration")
                .description("Tempo de resposta dos fornecedores")
                .tag("supplier", supplierName)
                .register(meterRegistry)
                .record(responseTime);
        log.debug("Métrica registrada: supplier.response.duration para {} = {}ms", supplierName, responseTime.toMillis());
    }

    // ===== MÉTRICAS DE IMAGENS =====

    /**
     * Registrar upload de imagem
     */
    public void recordImageUpload(String category, long fileSize) {
        Counter.builder("images.upload.total")
                .description("Total de imagens enviadas")
                .tag("category", category)
                .register(meterRegistry)
                .increment();
        
        Counter.builder("images.upload.size")
                .description("Tamanho total de imagens enviadas")
                .tag("category", category)
                .register(meterRegistry)
                .increment(fileSize);
        
        log.debug("Métrica registrada: images.upload para categoria {} com tamanho {}", category, fileSize);
    }

    /**
     * Registrar tempo de upload de imagem
     */
    public void recordImageUploadTime(String category, Duration uploadTime) {
        Timer.builder("images.upload.duration")
                .description("Tempo de upload de imagens")
                .tag("category", category)
                .register(meterRegistry)
                .record(uploadTime);
        log.debug("Métrica registrada: images.upload.duration para categoria {} = {}ms", category, uploadTime.toMillis());
    }

    // ===== MÉTRICAS DE USUÁRIOS =====

    /**
     * Registrar login de usuário
     */
    public void recordUserLogin(String userRole) {
        Counter.builder("users.login.total")
                .description("Total de logins de usuários")
                .tag("role", userRole)
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: users.login.total para role {}", userRole);
    }

    /**
     * Registrar criação de usuário
     */
    public void recordUserCreated(String userRole) {
        Counter.builder("users.created.total")
                .description("Total de usuários criados")
                .tag("role", userRole)
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: users.created.total para role {}", userRole);
    }

    // ===== MÉTRICAS DE PERFORMANCE =====

    /**
     * Registrar tempo de resposta da API
     */
    public void recordApiResponseTime(String endpoint, Duration responseTime) {
        Timer.builder("api.response.duration")
                .description("Tempo de resposta das APIs")
                .tag("endpoint", endpoint)
                .register(meterRegistry)
                .record(responseTime);
        log.debug("Métrica registrada: api.response.duration para {} = {}ms", endpoint, responseTime.toMillis());
    }

    /**
     * Registrar erro de API
     */
    public void recordApiError(String endpoint, String errorType) {
        Counter.builder("api.errors.total")
                .description("Total de erros das APIs")
                .tag("endpoint", endpoint)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: api.errors.total para {} com tipo {}", endpoint, errorType);
    }

    // ===== MÉTRICAS DE CACHE =====

    /**
     * Registrar hit do cache
     */
    public void recordCacheHit(String cacheName) {
        Counter.builder("cache.hits.total")
                .description("Total de hits do cache")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Registrar miss do cache
     */
    public void recordCacheMiss(String cacheName) {
        Counter.builder("cache.misses.total")
                .description("Total de misses do cache")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .increment();
    }

    // ===== MÉTRICAS DE FILAS =====

    /**
     * Registrar mensagem enviada para fila
     */
    public void recordQueueMessageSent(String queueName) {
        Counter.builder("queue.messages.sent.total")
                .description("Total de mensagens enviadas para filas")
                .tag("queue", queueName)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Registrar mensagem processada da fila
     */
    public void recordQueueMessageProcessed(String queueName, Duration processingTime) {
        Counter.builder("queue.messages.processed.total")
                .description("Total de mensagens processadas das filas")
                .tag("queue", queueName)
                .register(meterRegistry)
                .increment();
        
        Timer.builder("queue.message.processing.duration")
                .description("Tempo de processamento de mensagens das filas")
                .tag("queue", queueName)
                .register(meterRegistry)
                .record(processingTime);
    }

    // ===== MÉTRICAS DE NEGÓCIO =====

    /**
     * Registrar valor de pedido
     */
    public void recordOrderValue(double orderValue, String orderType) {
        Counter.builder("orders.value.total")
                .description("Valor total dos pedidos")
                .tag("order_type", orderType)
                .register(meterRegistry)
                .increment(orderValue);
    }

    /**
     * Registrar margem de lucro
     */
    public void recordProfitMargin(double margin, String orderType) {
        Counter.builder("orders.profit.margin")
                .description("Margem de lucro dos pedidos")
                .tag("order_type", orderType)
                .register(meterRegistry)
                .increment(margin);
    }

    /**
     * Registrar tempo de análise de pedido
     */
    public void recordOrderAnalysisTime(Duration analysisTime) {
        Timer.builder("order.analysis.duration")
                .description("Tempo de análise de pedidos")
                .register(meterRegistry)
                .record(analysisTime);
    }

    // ===== MÉTRICAS DE UPLOAD E STORAGE =====

    /**
     * Registrar upload de imagem para S3
     */
    public void recordImageUpload() {
        Counter.builder("upload.image.s3")
                .description("Uploads de imagens para S3")
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: upload.image.s3");
    }

    /**
     * Registrar upload de PDF para Google Drive
     */
    public void recordPdfUpload() {
        Counter.builder("upload.pdf.drive")
                .description("Uploads de PDFs para Google Drive")
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: upload.pdf.drive");
    }

    /**
     * Registrar upload de imagem de produto de catálogo
     */
    public void recordCatalogProductImageUpload() {
        Counter.builder("upload.catalog.product.image")
                .description("Uploads de imagens de produtos de catálogo")
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: upload.catalog.product.image");
    }

    /**
     * Registrar upload de imagem de pedido personalizado
     */
    public void recordCustomOrderImageUpload() {
        Counter.builder("upload.custom.order.image")
                .description("Uploads de imagens de pedidos personalizados")
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: upload.custom.order.image");
    }

    /**
     * Registrar upload de PDF de catálogo
     */
    public void recordCatalogPdfUpload() {
        Counter.builder("upload.catalog.pdf")
                .description("Uploads de PDFs de catálogos")
                .register(meterRegistry)
                .increment();
        log.debug("Métrica registrada: upload.catalog.pdf");
    }

    /**
     * Registrar erro de upload
     */
    public void recordUploadError(String type, String errorType) {
        Counter.builder("upload.error")
                .description("Erros de upload")
                .tag("upload_type", type)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
        log.warn("Erro de upload registrado: {} - {}", type, errorType);
    }
}
