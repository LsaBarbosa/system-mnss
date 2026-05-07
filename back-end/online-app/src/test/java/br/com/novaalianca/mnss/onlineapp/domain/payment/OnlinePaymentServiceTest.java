package br.com.novaalianca.mnss.onlineapp.domain.payment;

import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.onlineapp.application.payment.PaymentGatewayPort;
import br.com.novaalianca.mnss.onlineapp.application.payment.PaymentGatewayPort.PaymentGatewayResponse;
import br.com.novaalianca.mnss.onlineapp.domain.customer.OnlineCustomerEntity;
import br.com.novaalianca.mnss.onlineapp.domain.order.OnlineOrderEntity;
import br.com.novaalianca.mnss.onlineapp.domain.order.OnlineOrderRepository;
import br.com.novaalianca.mnss.onlineapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.onlineapp.domain.payment.dto.OnlinePaymentRequest;
import br.com.novaalianca.mnss.onlineapp.domain.sync.SyncEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnlinePaymentServiceTest {

    @Mock
    private OnlinePaymentRepository paymentRepository;
    @Mock
    private OnlineOrderRepository orderRepository;
    @Mock
    private PaymentGatewayPort paymentGatewayPort;
    @Mock
    private SyncEventRepository syncEventRepository;

    private OnlinePaymentService service;

    @BeforeEach
    void setUp() {
        service = new OnlinePaymentService(
                paymentRepository,
                orderRepository,
                paymentGatewayPort,
                syncEventRepository
        );
    }

    @Test
    void createPayment_ShouldPersistGatewayTransactionIdImmediately() {
        UUID orderId = UUID.randomUUID();
        OnlineOrderEntity order = mock(OnlineOrderEntity.class);
        OnlineCustomerEntity customer = mock(OnlineCustomerEntity.class);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getId()).thenReturn(orderId);
        when(order.getStatus()).thenReturn(OrderStatus.CREATED);
        when(order.getTotalAmount()).thenReturn(new BigDecimal("42.50"));
        when(order.getCustomer()).thenReturn(customer);
        when(customer.getName()).thenReturn("Cliente");
        when(customer.getEmail()).thenReturn("cliente@teste.com");
        when(customer.getPhone()).thenReturn("11999999999");
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse(
                "txn-123",
                "PENDING",
                "qr-base64",
                "qr-copy-paste",
                "https://gateway/payment/txn-123",
                Map.<String, Object>of("status", "PENDING")
        );
        when(paymentGatewayPort.createPayment(any())).thenReturn(gatewayResponse);

        service.createPayment(new OnlinePaymentRequest(orderId, PaymentMethod.ONLINE_PIX));

        ArgumentCaptor<OnlinePaymentEntity> paymentCaptor = ArgumentCaptor.forClass(OnlinePaymentEntity.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        OnlinePaymentEntity savedPayment = paymentCaptor.getValue();

        assertEquals("txn-123", savedPayment.getTransactionId());
        assertTrue(savedPayment.getWebhookPayload().contains("PENDING"));
    }

    @Test
    void processWebhook_WhenExpired_ShouldMarkPaymentAsExpired() {
        OnlinePaymentEntity payment = mock(OnlinePaymentEntity.class);
        OnlineOrderEntity order = mock(OnlineOrderEntity.class);

        when(paymentRepository.findByTransactionId("txn-123")).thenReturn(Optional.of(payment));
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(payment.getOrder()).thenReturn(order);

        service.processWebhook("txn-123", "EXPIRED", "{\"status\":\"EXPIRED\"}");

        verify(payment).markAsExpired();
        verify(paymentRepository).save(payment);
        verify(order).updatePaymentStatus(PaymentStatus.EXPIRED);
        verify(orderRepository).save(order);
        verify(syncEventRepository, never()).save(any());
    }

    @Test
    void processWebhook_WhenRefused_ShouldMarkPaymentAndOrderAsRefused() {
        OnlinePaymentEntity payment = mock(OnlinePaymentEntity.class);
        OnlineOrderEntity order = mock(OnlineOrderEntity.class);

        when(paymentRepository.findByTransactionId("txn-321")).thenReturn(Optional.of(payment));
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(payment.getOrder()).thenReturn(order);

        service.processWebhook("txn-321", "REFUSED", "{\"status\":\"REFUSED\"}");

        verify(payment).markAsRefused("txn-321", "{\"status\":\"REFUSED\"}");
        verify(order).updatePaymentStatus(PaymentStatus.REFUSED);
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
        verify(syncEventRepository, never()).save(any());
    }
}
