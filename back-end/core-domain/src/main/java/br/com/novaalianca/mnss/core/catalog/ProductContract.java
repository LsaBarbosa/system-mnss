package br.com.novaalianca.mnss.core.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Contrato compartilhado para Product entre local-app e online-app.
 * Ambas as aplicações implementam este contrato via suas entidades.
 */
public interface ProductContract {
    UUID getId();
    String getName();
    String getDescription();
    BigDecimal getPrice();
    BigDecimal getPromotionalPrice();
    boolean isActive();
    boolean isAvailable();
    boolean isSellOnPdv();
    CategoryContract getCategory();
    PreparationSector getPreparationSector();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
