package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.localapp.domain.payment.PaymentEntity;
import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import java.math.BigDecimal;
import java.util.UUID;

public record PdvSalePaymentResponse(UUID id, PaymentMethod method, BigDecimal amount) {
    static PdvSalePaymentResponse from(PaymentEntity payment) {
        return new PdvSalePaymentResponse(payment.getId(), payment.getMethod(), payment.getAmount());
    }
}
