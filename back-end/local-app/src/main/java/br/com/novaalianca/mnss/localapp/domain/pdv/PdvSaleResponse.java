package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.localapp.domain.order.DeliveryType;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderOrigin;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PdvSaleResponse(
        UUID id,
        Long orderNumber,
        OrderOrigin origin,
        OrderStatus status,
        PaymentStatus paymentStatus,
        DeliveryType deliveryType,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal deliveryFee,
        BigDecimal totalAmount,
        List<PdvSaleItemResponse> items,
        List<PdvSalePaymentResponse> payments,
        BigDecimal remainingAmount,
        Instant createdAt,
        Instant updatedAt) {
    static PdvSaleResponse from(OrderEntity order, List<PdvSaleItemResponse> items, List<PdvSalePaymentResponse> payments) {
        BigDecimal paidTotal = payments.stream()
                .map(PdvSalePaymentResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingAmount = order.getTotalAmount().subtract(paidTotal);
        if (remainingAmount.signum() < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        return new PdvSaleResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrigin(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getDeliveryType(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getDeliveryFee(),
                order.getTotalAmount(),
                items,
                payments,
                remainingAmount,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
