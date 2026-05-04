package br.com.novaalianca.mnss.localapp.domain.catalog;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        int displayOrder,
        String imageUrl,
        boolean active,
        boolean showOnline,
        boolean showOnPdv,
        boolean showOnWhatsapp,
        Instant createdAt,
        Instant updatedAt) {
    static CategoryResponse from(CategoryEntity category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getDisplayOrder(),
                category.getImageUrl(),
                category.isActive(),
                category.isShowOnline(),
                category.isShowOnPdv(),
                category.isShowOnWhatsapp(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
