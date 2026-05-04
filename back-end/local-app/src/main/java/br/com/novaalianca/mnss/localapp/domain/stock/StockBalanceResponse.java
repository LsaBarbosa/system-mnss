package br.com.novaalianca.mnss.localapp.domain.stock;

import java.math.BigDecimal;
import java.util.UUID;

public record StockBalanceResponse(UUID productId, String productName, BigDecimal quantity) {}
