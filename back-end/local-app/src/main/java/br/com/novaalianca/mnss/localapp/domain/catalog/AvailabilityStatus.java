package br.com.novaalianca.mnss.localapp.domain.catalog;

public enum AvailabilityStatus {
    AVAILABLE,
    UNAVAILABLE,
    AVAILABLE_UNTIL_STOCK_ENDS,
    PRE_ORDER_ONLY,
    PICKUP_ONLY,
    DELIVERY_ONLY,
    COUNTER_ONLY
}
