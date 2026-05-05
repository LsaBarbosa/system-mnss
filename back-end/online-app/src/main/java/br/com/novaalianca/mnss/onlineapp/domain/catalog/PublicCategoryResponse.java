package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import java.util.UUID;

public record PublicCategoryResponse(
        UUID id,
        String name,
        String description,
        int displayOrder,
        String imageUrl) {
    static PublicCategoryResponse from(OnlineCategoryEntity category) {
        return new PublicCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getDisplayOrder(),
                category.getImageUrl());
    }
}
