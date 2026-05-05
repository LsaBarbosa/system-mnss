package br.com.novaalianca.mnss.localapp.domain.pdv;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateDiscountRequest(@NotNull @PositiveOrZero BigDecimal amount) {}
