package br.com.novaalianca.mnss.onlineapp.domain.order.dto;

import br.com.novaalianca.mnss.onlineapp.domain.order.DeliveryType;
import br.com.novaalianca.mnss.onlineapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.onlineapp.domain.order.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record OnlineOrderResponse(
        UUID id,
        Long orderNumber,
        OrderStatus status,
        PaymentStatus paymentStatus,
        DeliveryType deliveryType,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal deliveryFee,
        BigDecimal totalAmount
) {}
