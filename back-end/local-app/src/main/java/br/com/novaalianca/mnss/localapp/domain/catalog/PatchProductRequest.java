package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.UUID;

public record PatchProductRequest(
        UUID categoryId,
        String name,
        String description,
        @DecimalMin("0.00") BigDecimal price,
        @DecimalMin("0.00") BigDecimal promotionalPrice,
        @DecimalMin("0.00") BigDecimal costPrice,
        String sku,
        String barcode,
        String imageUrl,
        UnitType unitType,
        PreparationSector preparationSector,
        Integer preparationTimeMinutes,
        Boolean active,
        Boolean available,
        Boolean sellOnPdv,
        Boolean sellOnline,
        Boolean sellOnWhatsapp,
        Boolean stockControlled) {}
