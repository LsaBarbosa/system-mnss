package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import java.math.BigDecimal;
import java.util.UUID;

public record PublicProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal promotionalPrice,
        String imageUrl,
        UnitType unitType,
        PreparationSector preparationSector,
        Integer preparationTimeMinutes) {
    public static PublicProductResponse from(OnlineProductEntity product) {
        return new PublicProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getPromotionalPrice(),
                product.getImageUrl(),
                product.getUnitType(),
                product.getPreparationSector(),
                product.getPreparationTimeMinutes());
    }
}
