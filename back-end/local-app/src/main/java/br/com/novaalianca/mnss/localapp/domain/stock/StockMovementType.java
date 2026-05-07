package br.com.novaalianca.mnss.localapp.domain.stock;

public enum StockMovementType {
    IN,
    OUT,
    ADJUSTMENT,
    ADJUSTMENT_POSITIVE,
    ADJUSTMENT_NEGATIVE,
    INVENTORY_COUNT,
    SALE,
    LOSS,
    RETURN
}
