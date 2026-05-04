package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.localapp.domain.payment.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CashRegisterSummaryResponse(
        CashRegisterResponse cashRegister,
        Map<PaymentMethod, BigDecimal> totalsByPaymentMethod,
        BigDecimal saleTotal,
        BigDecimal refundTotal,
        BigDecimal cashInTotal,
        BigDecimal cashOutTotal,
        BigDecimal adjustmentTotal,
        BigDecimal expectedAmount,
        BigDecimal closingAmount,
        BigDecimal differenceAmount,
        List<CashMovementResponse> movements) {}
