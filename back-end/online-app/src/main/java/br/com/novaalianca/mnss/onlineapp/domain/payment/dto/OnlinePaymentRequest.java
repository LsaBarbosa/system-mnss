package br.com.novaalianca.mnss.onlineapp.domain.payment.dto;

import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import java.util.UUID;

public record OnlinePaymentRequest(
    UUID orderId,
    PaymentMethod method
) {}
