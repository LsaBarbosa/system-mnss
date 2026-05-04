package br.com.novaalianca.mnss.localapp.domain.cash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentMethod;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CashRegisterServiceTest {
    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private CashMovementRepository cashMovementRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CashSyncEventService syncEventService;

    @Mock
    private AuditService auditService;

    @Test
    void openRequiresInitialAmountAndAuthenticatedUser() {
        assertThatThrownBy(() -> service().open(new CashRegisterOpenRequest(null, null), UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> service().open(
                        new CashRegisterOpenRequest(new BigDecimal("10.00"), null),
                        null))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void duplicateOpenCashRegisterIsBlocked() {
        UUID operatorId = UUID.randomUUID();
        when(cashRegisterRepository.existsByOperatorIdAndStatus(operatorId, CashRegisterStatus.OPEN)).thenReturn(true);

        assertThatThrownBy(() -> service().open(
                        new CashRegisterOpenRequest(new BigDecimal("20.00"), null),
                        operatorId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void openCreatesRegisterAuditAndSyncEvent() {
        UUID operatorId = UUID.randomUUID();
        when(cashRegisterRepository.existsByOperatorIdAndStatus(operatorId, CashRegisterStatus.OPEN)).thenReturn(false);
        when(cashRegisterRepository.save(any(CashRegisterEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        CashRegisterResponse response = service().open(
                new CashRegisterOpenRequest(new BigDecimal("30.00"), "Troco inicial"),
                operatorId);

        ArgumentCaptor<CashRegisterEntity> captor = ArgumentCaptor.forClass(CashRegisterEntity.class);
        verify(cashRegisterRepository).save(captor.capture());
        assertThat(response.operatorId()).isEqualTo(operatorId);
        assertThat(response.status()).isEqualTo(CashRegisterStatus.OPEN);
        assertThat(captor.getValue().getNotes()).isEqualTo("Troco inicial");
        verify(syncEventService).recordRegisterEvent("CASH_REGISTER_OPENED", captor.getValue());
    }

    @Test
    void currentReturnsOpenRegisterOrEmptyState() {
        UUID operatorId = UUID.randomUUID();
        CashRegisterEntity cashRegister = openRegister(operatorId);
        when(cashRegisterRepository.findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(operatorId, CashRegisterStatus.OPEN))
                .thenReturn(Optional.of(cashRegister))
                .thenReturn(Optional.empty());

        CurrentCashRegisterResponse open = service().current(operatorId);
        CurrentCashRegisterResponse empty = service().current(operatorId);

        assertThat(open.open()).isTrue();
        assertThat(open.cashRegister().operatorId()).isEqualTo(operatorId);
        assertThat(empty.open()).isFalse();
        assertThat(empty.cashRegister()).isNull();
    }

    @Test
    void cashOutRequiresReasonPositiveAmountAndOpenRegister() {
        UUID registerId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();

        assertThatThrownBy(() -> service().createManualMovement(
                        registerId,
                        new CashMovementRequest(CashMovementType.CASH_OUT, null, new BigDecimal("5.00"), " ", null),
                        actorUserId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> service().createManualMovement(
                        registerId,
                        new CashMovementRequest(CashMovementType.CASH_OUT, null, BigDecimal.ZERO, "Sangria", null),
                        actorUserId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        CashRegisterEntity closedRegister = openRegister(actorUserId);
        closedRegister.close(new BigDecimal("10.00"), new BigDecimal("10.00"), BigDecimal.ZERO, null);
        when(cashRegisterRepository.findById(registerId)).thenReturn(Optional.of(closedRegister));

        assertThatThrownBy(() -> service().createManualMovement(
                        registerId,
                        new CashMovementRequest(CashMovementType.CASH_OUT, null, new BigDecimal("1.00"), "Sangria", null),
                        actorUserId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(cashMovementRepository, never()).save(any(CashMovementEntity.class));
    }

    @Test
    void cashInCreatesMovementLinkedToRegister() {
        UUID registerId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        CashRegisterEntity cashRegister = openRegister(actorUserId);
        ReflectionTestUtils.setField(cashRegister, "id", registerId);
        when(cashRegisterRepository.findById(registerId)).thenReturn(Optional.of(cashRegister));
        when(cashMovementRepository.save(any(CashMovementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        CashMovementResponse response = service().createManualMovement(
                registerId,
                new CashMovementRequest(CashMovementType.CASH_IN, null, new BigDecimal("15.00"), "Suprimento", null),
                actorUserId);

        ArgumentCaptor<CashMovementEntity> captor = ArgumentCaptor.forClass(CashMovementEntity.class);
        verify(cashMovementRepository).save(captor.capture());
        assertThat(response.type()).isEqualTo(CashMovementType.CASH_IN);
        assertThat(response.cashRegisterId()).isEqualTo(registerId);
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(actorUserId);
        verify(syncEventService).recordMovementEvent(captor.getValue());
    }

    @Test
    void closeCalculatesExpectedDifferenceAndRequiresNotesOnDivergence() {
        UUID registerId = UUID.randomUUID();
        UUID operatorId = UUID.randomUUID();
        CashRegisterEntity cashRegister = openRegister(operatorId);
        ReflectionTestUtils.setField(cashRegister, "id", registerId);
        List<CashMovementEntity> movements = List.of(
                movement(cashRegister, CashMovementType.SALE, new BigDecimal("100.00"), PaymentMethod.CASH),
                movement(cashRegister, CashMovementType.CASH_IN, new BigDecimal("20.00"), null),
                movement(cashRegister, CashMovementType.CASH_OUT, new BigDecimal("10.00"), null));
        when(cashRegisterRepository.findById(registerId)).thenReturn(Optional.of(cashRegister));
        when(cashMovementRepository.findByCashRegisterIdOrderByCreatedAtAsc(registerId)).thenReturn(movements);

        assertThatThrownBy(() -> service().close(
                        registerId,
                        new CashRegisterCloseRequest(new BigDecimal("139.00"), " "),
                        operatorId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        when(cashRegisterRepository.save(any(CashRegisterEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        CashRegisterSummaryResponse summary = service().close(
                registerId,
                new CashRegisterCloseRequest(new BigDecimal("139.00"), "Faltou troco"),
                operatorId);

        assertThat(summary.expectedAmount()).isEqualByComparingTo("140.00");
        assertThat(summary.differenceAmount()).isEqualByComparingTo("-1.00");
        assertThat(summary.cashRegister().status()).isEqualTo(CashRegisterStatus.CLOSED);
        verify(syncEventService).recordRegisterEvent("CASH_REGISTER_CLOSED", cashRegister);
    }

    @Test
    void summaryGroupsByPaymentMethodAndIncludesManualMovements() {
        UUID registerId = UUID.randomUUID();
        UUID operatorId = UUID.randomUUID();
        CashRegisterEntity cashRegister = openRegister(operatorId);
        ReflectionTestUtils.setField(cashRegister, "id", registerId);
        List<CashMovementEntity> movements = List.of(
                movement(cashRegister, CashMovementType.SALE, new BigDecimal("50.00"), PaymentMethod.PIX),
                movement(cashRegister, CashMovementType.SALE, new BigDecimal("40.00"), PaymentMethod.CREDIT_CARD),
                movement(cashRegister, CashMovementType.CASH_OUT, new BigDecimal("5.00"), null),
                movement(cashRegister, CashMovementType.CASH_IN, new BigDecimal("8.00"), null));
        when(cashRegisterRepository.findById(registerId)).thenReturn(Optional.of(cashRegister));
        when(cashMovementRepository.findByCashRegisterIdOrderByCreatedAtAsc(registerId)).thenReturn(movements);

        CashRegisterSummaryResponse summary = service().summary(registerId);

        assertThat(summary.totalsByPaymentMethod()).containsEntry(PaymentMethod.PIX, new BigDecimal("50.00"));
        assertThat(summary.totalsByPaymentMethod()).containsEntry(PaymentMethod.CREDIT_CARD, new BigDecimal("40.00"));
        assertThat(summary.cashInTotal()).isEqualByComparingTo("8.00");
        assertThat(summary.cashOutTotal()).isEqualByComparingTo("5.00");
        assertThat(summary.expectedAmount()).isEqualByComparingTo("123.00");
    }

    private CashRegisterService service() {
        return new CashRegisterService(
                Optional.of(cashRegisterRepository),
                Optional.of(cashMovementRepository),
                Optional.of(orderRepository),
                syncEventService,
                auditService);
    }

    private CashRegisterEntity openRegister(UUID operatorId) {
        return new CashRegisterEntity(operatorId, new BigDecimal("30.00"), CashRegisterStatus.OPEN);
    }

    private CashMovementEntity movement(
            CashRegisterEntity cashRegister,
            CashMovementType type,
            BigDecimal amount,
            PaymentMethod paymentMethod) {
        return new CashMovementEntity(cashRegister, type, amount, paymentMethod, "Movimento", null, UUID.randomUUID());
    }
}
