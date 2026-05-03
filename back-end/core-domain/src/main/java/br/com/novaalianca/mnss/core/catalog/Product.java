package br.com.novaalianca.mnss.core.catalog;

import br.com.novaalianca.mnss.core.shared.Money;
import java.util.Objects;
import java.util.UUID;

public final class Product {
    private final UUID id;
    private final String name;
    private final Money price;
    private final UnitType unitType;
    private final PreparationSector preparationSector;
    private final boolean active;
    private final boolean available;
    private final boolean sellOnPdv;
    private final boolean sellOnline;
    private final boolean sellOnWhatsapp;

    public Product(
            UUID id,
            String name,
            Money price,
            UnitType unitType,
            PreparationSector preparationSector,
            boolean active,
            boolean available,
            boolean sellOnPdv,
            boolean sellOnline,
            boolean sellOnWhatsapp) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = requireText(name, "name");
        this.price = Objects.requireNonNull(price, "price must not be null");
        this.unitType = Objects.requireNonNull(unitType, "unitType must not be null");
        this.preparationSector =
                Objects.requireNonNull(preparationSector, "preparationSector must not be null");
        this.active = active;
        this.available = available;
        this.sellOnPdv = sellOnPdv;
        this.sellOnline = sellOnline;
        this.sellOnWhatsapp = sellOnWhatsapp;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Money price() {
        return price;
    }

    public boolean canSellOn(SalesChannel channel) {
        Objects.requireNonNull(channel, "channel must not be null");
        if (!active) {
            return false;
        }
        return switch (channel) {
            case PDV -> sellOnPdv;
            case SITE -> sellOnline && available;
            case WHATSAPP -> sellOnWhatsapp && available;
            case ALL -> canSellOn(SalesChannel.PDV)
                    && canSellOn(SalesChannel.SITE)
                    && canSellOn(SalesChannel.WHATSAPP);
        };
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
