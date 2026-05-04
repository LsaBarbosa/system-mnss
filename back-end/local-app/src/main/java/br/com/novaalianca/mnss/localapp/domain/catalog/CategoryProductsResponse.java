package br.com.novaalianca.mnss.localapp.domain.catalog;

import java.util.List;

public record CategoryProductsResponse(
        CategoryResponse category,
        List<ProductResponse> products) {}
