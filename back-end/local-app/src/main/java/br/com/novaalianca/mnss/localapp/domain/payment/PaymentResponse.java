package br.com.novaalianca.mnss.localapp.domain.payment;

import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount,
        String transactionId,
        String gateway,
        Instant paidAt,
        Instant canceledAt,
        OrderStatus orderStatus,
        PaymentStatus orderPaymentStatus,
        BigDecimal recordedAmount,
        BigDecimal remainingAmount,
        BigDecimal changeAmount,
        Instant createdAt,
        Instant updatedAt) {
    static PaymentResponse from(PaymentEntity payment, OrderEntity order, BigDecimal remainingAmount, BigDecimal changeAmount) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getTransactionId(),
                payment.getGateway(),
                payment.getPaidAt(),
                payment.getCanceledAt(),
                order.getStatus(),
                order.getPaymentStatus(),
                payment.getAmount(),
                remainingAmount,
                changeAmount,
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
