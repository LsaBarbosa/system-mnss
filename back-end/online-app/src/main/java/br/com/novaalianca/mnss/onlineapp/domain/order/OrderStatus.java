package br.com.novaalianca.mnss.onlineapp.domain.order;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    SENT_TO_STORE,
    RECEIVED_BY_STORE,
    ACCEPTED,
    IN_PREPARATION,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FINISHED,
    CANCELED
}
