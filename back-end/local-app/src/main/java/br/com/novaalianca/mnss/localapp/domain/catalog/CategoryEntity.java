package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.localapp.domain.shared.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class CategoryEntity extends BaseEntity {
    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private int displayOrder;

    @Column(columnDefinition = "text")
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean showOnline = true;

    @Column(nullable = false)
    private boolean showOnPdv = true;

    @Column(nullable = false)
    private boolean showOnWhatsapp = true;

    protected CategoryEntity() {}

    public CategoryEntity(String name) {
        this.name = DomainValidation.requireText(name, "name");
    }

    public void update(
            String name,
            String description,
            Integer displayOrder,
            String imageUrl,
            Boolean active,
            Boolean showOnline,
            Boolean showOnPdv,
            Boolean showOnWhatsapp) {
        if (name != null) {
            this.name = DomainValidation.requireText(name, "name");
        }
        if (description != null) {
            this.description = description;
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (active != null) {
            this.active = active;
        }
        if (showOnline != null) {
            this.showOnline = showOnline;
        }
        if (showOnPdv != null) {
            this.showOnPdv = showOnPdv;
        }
        if (showOnWhatsapp != null) {
            this.showOnWhatsapp = showOnWhatsapp;
        }
        touch();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isShowOnline() {
        return showOnline;
    }

    public boolean isShowOnPdv() {
        return showOnPdv;
    }

    public boolean isShowOnWhatsapp() {
        return showOnWhatsapp;
    }
}
