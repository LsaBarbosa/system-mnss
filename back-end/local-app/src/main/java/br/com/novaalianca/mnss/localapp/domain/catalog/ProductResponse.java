package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID categoryId,
        String name,
        String description,
        BigDecimal price,
        BigDecimal promotionalPrice,
        BigDecimal costPrice,
        String sku,
        String barcode,
        String imageUrl,
        UnitType unitType,
        PreparationSector preparationSector,
        Integer preparationTimeMinutes,
        boolean active,
        boolean available,
        boolean sellOnPdv,
        boolean sellOnline,
        boolean sellOnWhatsapp,
        Instant createdAt,
        Instant updatedAt) {
    static ProductResponse from(ProductEntity product) {
        CategoryEntity category = product.getCategory();
        return new ProductResponse(
                product.getId(),
                category == null ? null : category.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getPromotionalPrice(),
                product.getCostPrice(),
                product.getSku(),
                product.getBarcode(),
                product.getImageUrl(),
                product.getUnitType(),
                product.getPreparationSector(),
                product.getPreparationTimeMinutes(),
                product.isActive(),
                product.isAvailable(),
                product.isSellOnPdv(),
                product.isSellOnline(),
                product.isSellOnWhatsapp(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
