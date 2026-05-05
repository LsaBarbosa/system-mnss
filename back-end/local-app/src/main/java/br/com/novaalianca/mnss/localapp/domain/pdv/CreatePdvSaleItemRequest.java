package br.com.novaalianca.mnss.localapp.domain.pdv;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePdvSaleItemRequest(
        @NotNull UUID productId,
        @DecimalMin(value = "0.001", inclusive = true) BigDecimal quantity,
        String observation) {}
