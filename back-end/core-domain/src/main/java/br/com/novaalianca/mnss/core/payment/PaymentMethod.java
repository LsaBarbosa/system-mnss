package br.com.novaalianca.mnss.core.payment;

public enum PaymentMethod {
    CASH,
    PIX,
    CREDIT_CARD,
    DEBIT_CARD,
    ONLINE_PIX,
    ONLINE_CREDIT_CARD,
    ONLINE_DEBIT_CARD,
    MEAL_VOUCHER,
    MIXED;

    public boolean isOnline() {
        return this == ONLINE_PIX || this == ONLINE_CREDIT_CARD || this == ONLINE_DEBIT_CARD;
    }
}
