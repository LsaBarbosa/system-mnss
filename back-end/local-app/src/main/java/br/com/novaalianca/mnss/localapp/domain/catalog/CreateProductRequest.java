package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
        @NotNull UUID categoryId,
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @DecimalMin("0.00") BigDecimal promotionalPrice,
        @DecimalMin("0.00") BigDecimal costPrice,
        String sku,
        String barcode,
        String imageUrl,
        @NotNull UnitType unitType,
        @NotNull PreparationSector preparationSector,
        Integer preparationTimeMinutes,
        Boolean active,
        Boolean available,
        Boolean sellOnPdv,
        Boolean sellOnline,
        Boolean sellOnWhatsapp) {}
