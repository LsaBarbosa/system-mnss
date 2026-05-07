package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class OnlineProductEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private OnlineCategoryEntity category;

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
    private boolean active;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private boolean sellOnPdv;

    @Column(nullable = false)
    private boolean sellOnline;

    @Column(name = "sell_on_whatsapp", nullable = false)
    private boolean sellOnWhatsapp;

    @Column(nullable = false)
    private boolean stockControlled;

    protected OnlineProductEntity() {}

    public OnlineCategoryEntity getCategory() {
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

    public void updateAvailability(boolean available) {
        this.available = available;
        touch();
    }
}
