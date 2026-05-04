package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentMethod;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashRegisterService {
    private final Optional<CashRegisterRepository> cashRegisterRepository;
    private final Optional<CashMovementRepository> cashMovementRepository;
    private final Optional<OrderRepository> orderRepository;
    private final CashSyncEventService syncEventService;
    private final AuditService auditService;

    CashRegisterService(
            Optional<CashRegisterRepository> cashRegisterRepository,
            Optional<CashMovementRepository> cashMovementRepository,
            Optional<OrderRepository> orderRepository,
            CashSyncEventService syncEventService,
            AuditService auditService) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.cashMovementRepository = cashMovementRepository;
        this.orderRepository = orderRepository;
        this.syncEventService = syncEventService;
        this.auditService = auditService;
    }

    @Transactional
    public CashRegisterResponse open(CashRegisterOpenRequest request, UUID actorUserId) {
        requireActor(actorUserId);
        BigDecimal openingAmount = requireNonNegative(request.openingAmount(), "Valor inicial obrigatorio.");
        if (cashRegisterRepository().existsByOperatorIdAndStatus(actorUserId, CashRegisterStatus.OPEN)) {
            throw new BusinessException("CASH_REGISTER_ALREADY_OPEN", "Operador ja possui caixa aberto.", HttpStatus.CONFLICT);
        }

        CashRegisterEntity cashRegister = new CashRegisterEntity(actorUserId, openingAmount, CashRegisterStatus.OPEN);
        cashRegister.updateNotes(normalize(request.notes()));
        CashRegisterEntity saved = cashRegisterRepository().save(cashRegister);
        syncEventService.recordRegisterEvent("CASH_REGISTER_OPENED", saved);
        auditService.record(new AuditLogRequest(
                actorUserId,
                "CASH_REGISTER_OPENED",
                "CashRegister",
                saved.getId(),
                Map.of("openingAmount", openingAmount.toPlainString()),
                null));
        return CashRegisterResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CurrentCashRegisterResponse current(UUID actorUserId) {
        requireActor(actorUserId);
        return cashRegisterRepository()
                .findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(actorUserId, CashRegisterStatus.OPEN)
                .map(CurrentCashRegisterResponse::open)
                .orElseGet(CurrentCashRegisterResponse::empty);
    }

    @Transactional
    public CashMovementResponse createManualMovement(UUID cashRegisterId, CashMovementRequest request, UUID actorUserId) {
        CashMovementType type = requireManualMovementType(request.type());
        validateReason(request.description());
        return createMovement(
                cashRegisterId,
                type,
                request.paymentMethod(),
                request.amount(),
                request.description(),
                request.orderId(),
                actorUserId);
    }

    @Transactional
    public CashMovementResponse recordSaleMovement(
            UUID cashRegisterId,
            PaymentMethod paymentMethod,
            BigDecimal amount,
            UUID orderId,
            UUID actorUserId) {
        return createMovement(
                cashRegisterId,
                CashMovementType.SALE,
                paymentMethod,
                amount,
                "Venda",
                orderId,
                actorUserId);
    }

    @Transactional
    public CashRegisterSummaryResponse close(UUID cashRegisterId, CashRegisterCloseRequest request, UUID actorUserId) {
        requireActor(actorUserId);
        CashRegisterEntity cashRegister = findCashRegister(cashRegisterId);
        ensureOpen(cashRegister);
        BigDecimal closingAmount = requireNonNegative(request.closingAmount(), "Valor contado obrigatorio.");
        List<CashMovementEntity> movements = movements(cashRegisterId);
        BigDecimal expectedAmount = calculateExpected(cashRegister, movements);
        BigDecimal differenceAmount = closingAmount.subtract(expectedAmount);
        if (differenceAmount.compareTo(BigDecimal.ZERO) != 0 && isBlank(request.notes())) {
            throw new BusinessException(
                    "CASH_CLOSE_NOTES_REQUIRED",
                    "Justificativa obrigatoria para divergencia de caixa.",
                    HttpStatus.BAD_REQUEST);
        }

        cashRegister.close(closingAmount, expectedAmount, differenceAmount, normalize(request.notes()));
        CashRegisterEntity saved = cashRegisterRepository().save(cashRegister);
        syncEventService.recordRegisterEvent("CASH_REGISTER_CLOSED", saved);
        auditService.record(new AuditLogRequest(
                actorUserId,
                "CASH_REGISTER_CLOSED",
                "CashRegister",
                saved.getId(),
                Map.of(
                        "expectedAmount", expectedAmount.toPlainString(),
                        "closingAmount", closingAmount.toPlainString(),
                        "differenceAmount", differenceAmount.toPlainString()),
                null));
        return summaryFrom(saved, movements);
    }

    @Transactional(readOnly = true)
    public CashRegisterSummaryResponse summary(UUID cashRegisterId) {
        CashRegisterEntity cashRegister = findCashRegister(cashRegisterId);
        return summaryFrom(cashRegister, movements(cashRegisterId));
    }

    private CashMovementResponse createMovement(
            UUID cashRegisterId,
            CashMovementType type,
            PaymentMethod paymentMethod,
            BigDecimal amount,
            String description,
            UUID orderId,
            UUID actorUserId) {
        requireActor(actorUserId);
        BigDecimal positiveAmount = requirePositive(amount);
        CashRegisterEntity cashRegister = findCashRegister(cashRegisterId);
        ensureOpen(cashRegister);
        OrderEntity order = findOrder(orderId);

        CashMovementEntity movement = new CashMovementEntity(
                cashRegister,
                type,
                positiveAmount,
                paymentMethod,
                normalize(description),
                order,
                actorUserId);
        CashMovementEntity saved = cashMovementRepository().save(movement);
        syncEventService.recordMovementEvent(saved);
        auditService.record(new AuditLogRequest(
                actorUserId,
                "CASH_MOVEMENT_CREATED",
                "CashMovement",
                cashRegisterId,
                movementDetails(saved),
                null));
        return CashMovementResponse.from(saved);
    }

    private CashRegisterSummaryResponse summaryFrom(CashRegisterEntity cashRegister, List<CashMovementEntity> movements) {
        BigDecimal expectedAmount = cashRegister.getExpectedAmount() == null
                ? calculateExpected(cashRegister, movements)
                : cashRegister.getExpectedAmount();
        BigDecimal closingAmount = cashRegister.getClosingAmount();
        BigDecimal differenceAmount = cashRegister.getDifferenceAmount() == null
                ? closingAmount == null ? BigDecimal.ZERO : closingAmount.subtract(expectedAmount)
                : cashRegister.getDifferenceAmount();
        return new CashRegisterSummaryResponse(
                CashRegisterResponse.from(cashRegister),
                totalsByPaymentMethod(movements),
                totalByType(movements, CashMovementType.SALE),
                totalByType(movements, CashMovementType.REFUND),
                totalByType(movements, CashMovementType.CASH_IN),
                totalByType(movements, CashMovementType.CASH_OUT),
                totalByType(movements, CashMovementType.ADJUSTMENT),
                expectedAmount,
                closingAmount,
                differenceAmount,
                movements.stream().map(CashMovementResponse::from).toList());
    }

    private BigDecimal calculateExpected(CashRegisterEntity cashRegister, List<CashMovementEntity> movements) {
        BigDecimal movementTotal = movements.stream()
                .map(movement -> signedAmount(movement.getType(), movement.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return cashRegister.getOpeningAmount().add(movementTotal);
    }

    private Map<PaymentMethod, BigDecimal> totalsByPaymentMethod(List<CashMovementEntity> movements) {
        Map<PaymentMethod, BigDecimal> totals = new EnumMap<>(PaymentMethod.class);
        for (PaymentMethod paymentMethod : PaymentMethod.values()) {
            totals.put(paymentMethod, BigDecimal.ZERO);
        }
        movements.stream()
                .filter(movement -> movement.getPaymentMethod() != null)
                .forEach(movement -> totals.merge(
                        movement.getPaymentMethod(),
                        signedAmount(movement.getType(), movement.getAmount()),
                        BigDecimal::add));
        return totals;
    }

    private BigDecimal totalByType(List<CashMovementEntity> movements, CashMovementType type) {
        return movements.stream()
                .filter(movement -> movement.getType() == type)
                .map(CashMovementEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal signedAmount(CashMovementType type, BigDecimal amount) {
        return switch (type) {
            case CASH_OUT, REFUND -> amount.negate();
            case SALE, CASH_IN, ADJUSTMENT -> amount;
        };
    }

    private Map<String, Object> movementDetails(CashMovementEntity movement) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("cashRegisterId", movement.getCashRegister().getId().toString());
        details.put("type", movement.getType().name());
        details.put("paymentMethod", movement.getPaymentMethod() == null ? null : movement.getPaymentMethod().name());
        details.put("amount", movement.getAmount().toPlainString());
        details.put("description", movement.getDescription());
        details.put("orderId", movement.getOrder() == null ? null : movement.getOrder().getId().toString());
        return details;
    }

    private CashMovementType requireManualMovementType(CashMovementType type) {
        if (type != CashMovementType.CASH_IN && type != CashMovementType.CASH_OUT) {
            throw new BusinessException(
                    "INVALID_CASH_MOVEMENT_TYPE",
                    "Use CASH_IN ou CASH_OUT para movimentacao manual.",
                    HttpStatus.BAD_REQUEST);
        }
        return type;
    }

    private BigDecimal requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("INVALID_CASH_AMOUNT", "Valor deve ser positivo.", HttpStatus.BAD_REQUEST);
        }
        return amount;
    }

    private BigDecimal requireNonNegative(BigDecimal amount, String message) {
        if (amount == null || amount.signum() < 0) {
            throw new BusinessException("INVALID_CASH_AMOUNT", message, HttpStatus.BAD_REQUEST);
        }
        return amount;
    }

    private void validateReason(String reason) {
        if (isBlank(reason)) {
            throw new BusinessException("CASH_MOVEMENT_REASON_REQUIRED", "Motivo obrigatorio.", HttpStatus.BAD_REQUEST);
        }
    }

    private void requireActor(UUID actorUserId) {
        if (actorUserId == null) {
            throw new BusinessException("USER_REQUIRED", "Usuario responsavel obrigatorio.", HttpStatus.BAD_REQUEST);
        }
    }

    private CashRegisterEntity findCashRegister(UUID cashRegisterId) {
        if (cashRegisterId == null) {
            throw new BusinessException("CASH_REGISTER_REQUIRED", "Caixa obrigatorio.", HttpStatus.BAD_REQUEST);
        }
        return cashRegisterRepository()
                .findById(cashRegisterId)
                .orElseThrow(() -> new BusinessException("CASH_REGISTER_NOT_FOUND", "Caixa nao encontrado.", HttpStatus.NOT_FOUND));
    }

    private void ensureOpen(CashRegisterEntity cashRegister) {
        if (cashRegister.getStatus() != CashRegisterStatus.OPEN) {
            throw new BusinessException("CASH_REGISTER_CLOSED", "Caixa fechado nao recebe movimentacao.", HttpStatus.BAD_REQUEST);
        }
    }

    private List<CashMovementEntity> movements(UUID cashRegisterId) {
        return cashMovementRepository().findByCashRegisterIdOrderByCreatedAtAsc(cashRegisterId);
    }

    private OrderEntity findOrder(UUID orderId) {
        if (orderId == null) {
            return null;
        }
        return orderRepository()
                .findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Pedido nao encontrado.", HttpStatus.NOT_FOUND));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private CashRegisterRepository cashRegisterRepository() {
        return cashRegisterRepository
                .orElseThrow(() -> new IllegalStateException("Cash register repository is not available."));
    }

    private CashMovementRepository cashMovementRepository() {
        return cashMovementRepository
                .orElseThrow(() -> new IllegalStateException("Cash movement repository is not available."));
    }

    private OrderRepository orderRepository() {
        return orderRepository.orElseThrow(() -> new IllegalStateException("Order repository is not available."));
    }
}
