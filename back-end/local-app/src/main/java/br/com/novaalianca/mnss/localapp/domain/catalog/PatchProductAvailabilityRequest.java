package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PatchProductAvailabilityRequest(
        @NotNull AvailabilityStatus status,
        SalesChannel channel,
        BigDecimal availableQuantity,
        String reason) {}
