package br.com.novaalianca.mnss.core.payment;

public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    PAID,
    PARTIALLY_PAID,
    REFUSED,
    CANCELED,
    REFUNDED,
    EXPIRED
}
