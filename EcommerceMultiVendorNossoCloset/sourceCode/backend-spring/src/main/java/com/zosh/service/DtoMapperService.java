package com.zosh.service;

import com.zosh.dto.*;
import com.zosh.model.*;
import com.zosh.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para mapeamento entre entidades e DTOs
 * Fase 1 - Semana 4: Desenvolvimento de APIs REST
 */
@Slf4j
@Service
public class DtoMapperService {

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private CollectiveOrderRepository collectiveOrderRepository;

    @Autowired
    private CustomOrderRepository customOrderRepository;

    // ===== SUPPLIER MAPPINGS =====

    public Supplier toSupplierEntity(SupplierRequestDto dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContactName(dto.getContactName());
        supplier.setWhatsapp(dto.getWhatsapp());
        supplier.setWebsite(dto.getWebsite());
        supplier.setEmail(dto.getEmail());
        supplier.setMinimumOrderValue(dto.getMinimumOrderValue());
        supplier.setDeliveryTimeDays(dto.getDeliveryTimeDays());
        supplier.setAdminFeePercentage(dto.getAdminFeePercentage());
        supplier.setPerformanceRating(dto.getPerformanceRating() != null ? dto.getPerformanceRating() : 5);
        supplier.setNotes(dto.getNotes());
        
        if (dto.getCategories() != null) {
            supplier.getCategories().addAll(dto.getCategories());
        }
        
        return supplier;
    }

    public SupplierResponseDto toSupplierDto(Supplier supplier) {
        SupplierResponseDto dto = new SupplierResponseDto();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setContactName(supplier.getContactName());
        dto.setWhatsapp(supplier.getWhatsapp());
        dto.setWebsite(supplier.getWebsite());
        dto.setEmail(supplier.getEmail());
        dto.setMinimumOrderValue(supplier.getMinimumOrderValue());
        dto.setDeliveryTimeDays(supplier.getDeliveryTimeDays());
        dto.setAdminFeePercentage(supplier.getAdminFeePercentage());
        dto.setCategories(supplier.getCategories());
        dto.setPerformanceRating(supplier.getPerformanceRating());
        dto.setStatus(supplier.getStatus());
        dto.setNotes(supplier.getNotes());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());
        dto.setCreatedBy(supplier.getCreatedBy());
        dto.setUpdatedBy(supplier.getUpdatedBy());
        
        // Campos calculados
        dto.setActive(supplier.isActive());
        dto.setCatalogCount(catalogRepository.countBySupplier(supplier));
        dto.setActiveOrdersCount((int) collectiveOrderRepository.countBySupplier(supplier));
        dto.setTotalOrdersValue(collectiveOrderRepository.getTotalValueBySupplier(supplier));
        
        return dto;
    }

    public void updateSupplierFromDto(Supplier supplier, SupplierRequestDto dto) {
        supplier.setName(dto.getName());
        supplier.setContactName(dto.getContactName());
        supplier.setWhatsapp(dto.getWhatsapp());
        supplier.setWebsite(dto.getWebsite());
        supplier.setEmail(dto.getEmail());
        supplier.setMinimumOrderValue(dto.getMinimumOrderValue());
        supplier.setDeliveryTimeDays(dto.getDeliveryTimeDays());
        supplier.setAdminFeePercentage(dto.getAdminFeePercentage());
        supplier.setPerformanceRating(dto.getPerformanceRating() != null ? dto.getPerformanceRating() : supplier.getPerformanceRating());
        supplier.setNotes(dto.getNotes());
        
        // Atualizar categorias
        supplier.getCategories().clear();
        if (dto.getCategories() != null) {
            supplier.getCategories().addAll(dto.getCategories());
        }
    }

    // ===== CUSTOM ORDER MAPPINGS =====

    public CustomOrder toCustomOrderEntity(CustomOrderRequestDto dto, User client) {
        CustomOrder order = new CustomOrder();
        order.setClient(client);
        order.setProductImageUrl(dto.getProductImageUrl());
        order.setReferenceCode(dto.getReferenceCode());
        order.setDescription(dto.getDescription());
        order.setPreferredColor(dto.getPreferredColor());
        order.setSize(dto.getSize());
        order.setCategory(dto.getCategory());
        order.setObservations(dto.getObservations());
        order.setEstimatedPrice(dto.getEstimatedPrice());
        order.setUrgency(dto.getUrgency() != null ? dto.getUrgency() : order.getUrgency());
        order.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        
        if (dto.getAlternativeColors() != null) {
            order.getAlternativeColors().addAll(dto.getAlternativeColors());
        }
        
        return order;
    }

    public CustomOrderResponseDto toCustomOrderDto(CustomOrder order) {
        CustomOrderResponseDto dto = new CustomOrderResponseDto();
        dto.setId(order.getId());
        dto.setProductImageUrl(order.getProductImageUrl());
        dto.setReferenceCode(order.getReferenceCode());
        dto.setDescription(order.getDescription());
        dto.setPreferredColor(order.getPreferredColor());
        dto.setAlternativeColors(order.getAlternativeColors());
        dto.setSize(order.getSize());
        dto.setCategory(order.getCategory());
        dto.setObservations(order.getObservations());
        dto.setEstimatedPrice(order.getEstimatedPrice());
        dto.setFinalPrice(order.getFinalPrice());
        dto.setStatus(order.getStatus());
        dto.setUrgency(order.getUrgency());
        dto.setQuantity(order.getQuantity());
        dto.setAdminNotes(order.getAdminNotes());
        dto.setAnalyzedBy(order.getAnalyzedBy());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setAnalyzedAt(order.getAnalyzedAt());
        dto.setConfirmedAt(order.getConfirmedAt());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setDeliveredAt(order.getDeliveredAt());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setUpdatedBy(order.getUpdatedBy());
        
        // Relacionamentos
        if (order.getClient() != null) {
            dto.setClient(new CustomOrderResponseDto.UserSummaryDto(
                order.getClient().getId(),
                order.getClient().getFullName(),
                order.getClient().getEmail(),
                order.getClient().getWhatsapp()
            ));
        }
        
        if (order.getSupplier() != null) {
            dto.setSupplier(new CustomOrderResponseDto.SupplierSummaryDto(
                order.getSupplier().getId(),
                order.getSupplier().getName(),
                order.getSupplier().getContactName(),
                order.getSupplier().getDeliveryTimeDays(),
                order.getSupplier().getAdminFeePercentage()
            ));
        }
        
        if (order.getCollectiveOrder() != null) {
            dto.setCollectiveOrder(new CustomOrderResponseDto.CollectiveOrderSummaryDto(
                order.getCollectiveOrder().getId(),
                order.getCollectiveOrder().getMinimumValue(),
                order.getCollectiveOrder().getCurrentValue(),
                order.getCollectiveOrder().getPaymentDeadline()
            ));
        }
        
        // Campos calculados
        dto.setTotalValue(order.getTotalValue());
        dto.setDaysOld(order.getDaysOld());
        dto.setUrgent(order.isUrgent());
        dto.setPending(order.isPending());
        dto.setConfirmed(order.isConfirmed());
        dto.setCancelled(order.isCancelled());
        dto.setDelivered(order.isDelivered());
        
        return dto;
    }

    public void updateCustomOrderFromDto(CustomOrder order, CustomOrderRequestDto dto) {
        order.setProductImageUrl(dto.getProductImageUrl());
        order.setReferenceCode(dto.getReferenceCode());
        order.setDescription(dto.getDescription());
        order.setPreferredColor(dto.getPreferredColor());
        order.setSize(dto.getSize());
        order.setCategory(dto.getCategory());
        order.setObservations(dto.getObservations());
        order.setEstimatedPrice(dto.getEstimatedPrice());
        order.setUrgency(dto.getUrgency() != null ? dto.getUrgency() : order.getUrgency());
        order.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : order.getQuantity());
        
        // Atualizar cores alternativas
        order.getAlternativeColors().clear();
        if (dto.getAlternativeColors() != null) {
            order.getAlternativeColors().addAll(dto.getAlternativeColors());
        }
    }

    // ===== UTILITY METHODS =====

    public List<SupplierResponseDto> toSupplierDtoList(List<Supplier> suppliers) {
        return suppliers.stream()
                .map(this::toSupplierDto)
                .collect(Collectors.toList());
    }

    public List<CustomOrderResponseDto> toCustomOrderDtoList(List<CustomOrder> orders) {
        return orders.stream()
                .map(this::toCustomOrderDto)
                .collect(Collectors.toList());
    }
}
