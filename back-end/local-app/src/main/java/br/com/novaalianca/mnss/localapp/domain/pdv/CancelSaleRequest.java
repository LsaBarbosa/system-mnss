package br.com.novaalianca.mnss.localapp.domain.pdv;

import jakarta.validation.constraints.NotBlank;

public record CancelSaleRequest(@NotBlank String reason) {}
