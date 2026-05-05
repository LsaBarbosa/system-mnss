package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import java.util.List;

public record PublicMenuResponse(
        PublicCategoryResponse category,
        List<PublicProductResponse> products) {}
