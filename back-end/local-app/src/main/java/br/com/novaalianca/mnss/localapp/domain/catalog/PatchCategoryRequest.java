package br.com.novaalianca.mnss.localapp.domain.catalog;

public record PatchCategoryRequest(
        String name,
        String description,
        Integer displayOrder,
        String imageUrl,
        Boolean active,
        Boolean showOnline,
        Boolean showOnPdv,
        Boolean showOnWhatsapp) {}
