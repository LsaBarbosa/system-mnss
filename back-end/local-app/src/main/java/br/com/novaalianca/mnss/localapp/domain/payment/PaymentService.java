package br.com.novaalianca.mnss.localapp.domain.payment;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterEntity;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterRepository;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterService;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterStatus;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderOrigin;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.localapp.domain.stock.StockService;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final Optional<OrderRepository> orderRepository;
    private final Optional<OrderItemRepository> orderItemRepository;
    private final Optional<PaymentRepository> paymentRepository;
    private final Optional<CashRegisterRepository> cashRegisterRepository;
    private final CashRegisterService cashRegisterService;
    private final StockService stockService;
    private final AuditService auditService;

    PaymentService(
            Optional<OrderRepository> orderRepository,
            Optional<OrderItemRepository> orderItemRepository,
            Optional<PaymentRepository> paymentRepository,
            Optional<CashRegisterRepository> cashRegisterRepository,
            CashRegisterService cashRegisterService,
            StockService stockService,
            AuditService auditService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.cashRegisterRepository = cashRegisterRepository;
        this.cashRegisterService = cashRegisterService;
        this.stockService = stockService;
        this.auditService = auditService;
    }

    @Transactional
    public PaymentResponse payOrder(UUID orderId, CreatePaymentRequest request, UUID actorUserId) {
        requireActor(actorUserId);
        PaymentMethod method = requireMethod(request.method());
        BigDecimal amount = money(requirePositiveAmount(request.amount()));
        OrderEntity order = editablePdvOrder(orderId);
        List<OrderItemEntity> items = orderItemRepository().findByOrderIdOrderByCreatedAtAsc(order.getId());
        if (items.isEmpty()) {
            throw new BusinessException("EMPTY_SALE", "Venda sem itens nao pode ser finalizada.", HttpStatus.BAD_REQUEST);
        }
        BigDecimal paidTotal = paymentRepository().findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingAmount = money(order.getTotalAmount()).subtract(paidTotal);

        if (remainingAmount.signum() <= 0) {
            throw new BusinessException("ORDER_ALREADY_PAID", "Pedido ja esta totalmente pago.", HttpStatus.CONFLICT);
        }

        BigDecimal requestAmount = money(requirePositiveAmount(request.amount()));
        BigDecimal amountToRecord = requestAmount;
        BigDecimal changeAmount = BigDecimal.ZERO;

        if (requestAmount.compareTo(remainingAmount) > 0) {
            if (method == PaymentMethod.CASH) {
                amountToRecord = remainingAmount;
                changeAmount = requestAmount.subtract(remainingAmount);
            } else {
                throw new BusinessException("PAYMENT_AMOUNT_MISMATCH", "Valor do pagamento excede o saldo devedor.", HttpStatus.BAD_REQUEST);
            }
        }

        CashRegisterEntity cashRegister = openCashRegister(actorUserId);
        PaymentEntity payment = new PaymentEntity(order, method, PaymentStatus.PAID, amountToRecord);
        payment.markPaid(request.transactionId(), request.gateway());
        PaymentEntity saved = paymentRepository().save(payment);

        cashRegisterService.recordSaleMovement(cashRegister.getId(), method, amountToRecord, order.getId(), actorUserId);

        BigDecimal newRemainingAmount = remainingAmount.subtract(amountToRecord);
        OrderEntity savedOrder = order;

        if (newRemainingAmount.signum() <= 0) {
            recordStockMovements(order, items, actorUserId);
            order.markPaid(nextPaidStatus(items));
            savedOrder = orderRepository().save(order);
        }

        auditService.record(new AuditLogRequest(
                actorUserId,
                "ORDER_PAYMENT_RECORDED",
                "Order",
                order.getId(),
                paymentDetails(saved, savedOrder, newRemainingAmount, changeAmount),
                null));
        return PaymentResponse.from(saved, savedOrder, newRemainingAmount, changeAmount);
    }

    private OrderEntity editablePdvOrder(UUID orderId) {
        if (orderId == null) {
            throw new BusinessException("ORDER_REQUIRED", "Pedido obrigatorio.", HttpStatus.BAD_REQUEST);
        }
        OrderEntity order = orderRepository()
                .findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Pedido nao encontrado.", HttpStatus.NOT_FOUND));
        if (order.getOrigin() != OrderOrigin.PDV || order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("ORDER_NOT_PAYABLE", "Pedido nao esta aberto para pagamento.", HttpStatus.BAD_REQUEST);
        }
        return order;
    }

    private void recordStockMovements(OrderEntity order, List<OrderItemEntity> items, UUID actorUserId) {
        items.stream()
                .filter(item -> item.getProduct() != null)
                .forEach(item -> stockService.recordSaleMovement(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        order.getId(),
                        actorUserId));
    }

    private OrderStatus nextPaidStatus(List<OrderItemEntity> items) {
        boolean requiresPreparation = items.stream()
                .anyMatch(item -> item.getPreparationSector() != PreparationSector.SEM_PREPARO);
        return requiresPreparation ? OrderStatus.SENT_TO_STORE : OrderStatus.PAID;
    }

    private CashRegisterEntity openCashRegister(UUID actorUserId) {
        return cashRegisterRepository()
                .findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(actorUserId, CashRegisterStatus.OPEN)
                .orElseThrow(() -> new BusinessException(
                        "OPEN_CASH_REGISTER_REQUIRED",
                        "Abra o caixa antes de finalizar venda.",
                        HttpStatus.BAD_REQUEST));
    }

    private PaymentMethod requireMethod(PaymentMethod method) {
        if (method == null) {
            throw new BusinessException("PAYMENT_METHOD_REQUIRED", "Forma de pagamento obrigatoria.", HttpStatus.BAD_REQUEST);
        }
        return method;
    }

    private BigDecimal requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("INVALID_PAYMENT_AMOUNT", "Valor do pagamento deve ser positivo.", HttpStatus.BAD_REQUEST);
        }
        return amount;
    }

    private void requireActor(UUID actorUserId) {
        if (actorUserId == null) {
            throw new BusinessException("USER_REQUIRED", "Usuario responsavel obrigatorio.", HttpStatus.BAD_REQUEST);
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> paymentDetails(PaymentEntity payment, OrderEntity order, BigDecimal remainingAmount, BigDecimal changeAmount) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("paymentId", payment.getId() == null ? null : payment.getId().toString());
        details.put("method", payment.getMethod().name());
        details.put("recordedAmount", payment.getAmount().toPlainString());
        details.put("remainingAmount", remainingAmount.toPlainString());
        details.put("changeAmount", changeAmount.toPlainString());
        details.put("orderStatus", order.getStatus().name());
        details.put("paymentStatus", order.getPaymentStatus().name());
        return details;
    }

    private OrderRepository orderRepository() {
        return orderRepository.orElseThrow(() -> new IllegalStateException("Order repository is not available."));
    }

    private OrderItemRepository orderItemRepository() {
        return orderItemRepository
                .orElseThrow(() -> new IllegalStateException("Order item repository is not available."));
    }

    private PaymentRepository paymentRepository() {
        return paymentRepository.orElseThrow(() -> new IllegalStateException("Payment repository is not available."));
    }

    private CashRegisterRepository cashRegisterRepository() {
        return cashRegisterRepository
                .orElseThrow(() -> new IllegalStateException("Cash register repository is not available."));
    }
}
