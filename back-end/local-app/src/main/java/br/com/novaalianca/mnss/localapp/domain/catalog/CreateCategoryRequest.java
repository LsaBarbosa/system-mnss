package br.com.novaalianca.mnss.localapp.domain.catalog;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank String name,
        String description,
        Integer displayOrder,
        String imageUrl,
        Boolean active,
        Boolean showOnline,
        Boolean showOnPdv,
        Boolean showOnWhatsapp) {}
