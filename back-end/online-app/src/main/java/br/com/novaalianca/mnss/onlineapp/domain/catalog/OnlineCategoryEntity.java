package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class OnlineCategoryEntity extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private int displayOrder;

    @Column(columnDefinition = "text")
    private String imageUrl;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean showOnline;

    protected OnlineCategoryEntity() {}

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
}
