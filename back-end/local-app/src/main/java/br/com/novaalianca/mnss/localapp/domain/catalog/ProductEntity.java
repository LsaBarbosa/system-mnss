package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "products")
public class ProductEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal promotionalPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(length = 80)
    private String sku;

    @Column(length = 80)
    private String barcode;

    @Column(columnDefinition = "text")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UnitType unitType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private PreparationSector preparationSector;

    private Integer preparationTimeMinutes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private boolean sellOnPdv = true;

    @Column(nullable = false)
    private boolean sellOnline = true;

    @Column(nullable = false)
    private boolean sellOnWhatsapp = true;

    @Column(nullable = false)
    private boolean stockControlled = false;

    protected ProductEntity() {}

    public ProductEntity(
            CategoryEntity category,
            String name,
            BigDecimal price,
            UnitType unitType,
            PreparationSector preparationSector) {
        this.category = category;
        this.name = DomainValidation.requireText(name, "name");
        this.price = DomainValidation.requireNonNegative(price, "price");
        this.unitType = Objects.requireNonNull(unitType, "unitType must not be null");
        this.preparationSector = Objects.requireNonNull(preparationSector, "preparationSector must not be null");
    }

    public void update(
            CategoryEntity category,
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
            Boolean active,
            Boolean available,
            Boolean sellOnPdv,
            Boolean sellOnline,
            Boolean sellOnWhatsapp,
            Boolean stockControlled) {
        if (category != null) {
            this.category = category;
        }
        if (name != null) {
            this.name = DomainValidation.requireText(name, "name");
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            this.price = DomainValidation.requireNonNegative(price, "price");
        }
        if (promotionalPrice != null) {
            this.promotionalPrice = DomainValidation.requireNonNegative(promotionalPrice, "promotionalPrice");
        }
        if (costPrice != null) {
            this.costPrice = DomainValidation.requireNonNegative(costPrice, "costPrice");
        }
        if (sku != null) {
            this.sku = sku;
        }
        if (barcode != null) {
            this.barcode = barcode;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (unitType != null) {
            this.unitType = unitType;
        }
        if (preparationSector != null) {
            this.preparationSector = preparationSector;
        }
        if (preparationTimeMinutes != null) {
            this.preparationTimeMinutes = preparationTimeMinutes;
        }
        if (active != null) {
            this.active = active;
        }
        if (available != null) {
            this.available = available;
        }
        if (sellOnPdv != null) {
            this.sellOnPdv = sellOnPdv;
        }
        if (sellOnline != null) {
            this.sellOnline = sellOnline;
        }
        if (sellOnWhatsapp != null) {
            this.sellOnWhatsapp = sellOnWhatsapp;
        }
        if (stockControlled != null) {
            this.stockControlled = stockControlled;
        }
        touch();
    }

    public void changeAvailability(boolean available) {
        this.available = available;
        touch();
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getPromotionalPrice() {
        return promotionalPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public String getSku() {
        return sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public PreparationSector getPreparationSector() {
        return preparationSector;
    }

    public Integer getPreparationTimeMinutes() {
        return preparationTimeMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isSellOnPdv() {
        return sellOnPdv;
    }

    public boolean isSellOnline() {
        return sellOnline;
    }

    public boolean isSellOnWhatsapp() {
        return sellOnWhatsapp;
    }

    public boolean isStockControlled() {
        return stockControlled;
    }
}
