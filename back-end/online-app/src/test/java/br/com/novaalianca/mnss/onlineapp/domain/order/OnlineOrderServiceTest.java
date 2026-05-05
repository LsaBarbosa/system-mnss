package br.com.novaalianca.mnss.onlineapp.domain.order;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductAvailabilityEntity;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductRepository;
import br.com.novaalianca.mnss.onlineapp.domain.customer.OnlineCustomerAddressRepository;
import br.com.novaalianca.mnss.onlineapp.domain.customer.OnlineCustomerEntity;
import br.com.novaalianca.mnss.onlineapp.domain.customer.OnlineCustomerRepository;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.CreateOnlineOrderRequest;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.CustomerRequest;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.OnlineOrderItemRequest;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.OnlineOrderResponse;
import br.com.novaalianca.mnss.onlineapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OnlineOrderServiceTest {

    @Mock
    private OnlineOrderRepository orderRepository;
    @Mock
    private OnlineCustomerRepository customerRepository;
    @Mock
    private OnlineCustomerAddressRepository addressRepository;
    @Mock
    private OnlineProductRepository productRepository;
    @Mock
    private SyncEventRepository syncEventRepository;

    private OnlineOrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OnlineOrderService(
                orderRepository,
                customerRepository,
                addressRepository,
                productRepository,
                syncEventRepository
        );
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        UUID productId = UUID.randomUUID();
        CustomerRequest customerReq = new CustomerRequest("John Doe", "123456789", "john@example.com", null);
        OnlineOrderItemRequest itemReq = new OnlineOrderItemRequest(productId, BigDecimal.ONE, "No onions");
        CreateOnlineOrderRequest request = new CreateOnlineOrderRequest(
                customerReq,
                DeliveryType.PICKUP,
                null,
                List.of(itemReq),
                "Order notes",
                br.com.novaalianca.mnss.core.payment.PaymentMethod.CASH
        );

        OnlineProductEntity product = mock(OnlineProductEntity.class);
        lenient().when(product.getId()).thenReturn(productId);
        lenient().when(product.getName()).thenReturn("Burger");
        lenient().when(product.getPrice()).thenReturn(new BigDecimal("20.00"));
        lenient().when(product.isActive()).thenReturn(true);
        lenient().when(product.isSellOnline()).thenReturn(true);
        lenient().when(product.isAvailable()).thenReturn(true);
        lenient().when(product.getPreparationSector()).thenReturn(PreparationSector.CHAPA);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(customerRepository.findByPhone(any())).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(orderRepository.save(any())).thenAnswer(i -> {
            OnlineOrderEntity order = (OnlineOrderEntity) i.getArguments()[0];
            // Simulate JPA setting the ID
            return order;
        });

        // When
        OnlineOrderResponse response = orderService.createOnlineOrder(request);

        // Then
        assertNotNull(response);
        assertEquals(OrderStatus.SENT_TO_STORE, response.status());
        assertEquals(new BigDecimal("20.00"), response.totalAmount());
        assertEquals(br.com.novaalianca.mnss.core.payment.PaymentMethod.CASH, response.paymentMethod());
        
        verify(customerRepository).save(any(OnlineCustomerEntity.class));
        verify(orderRepository).save(any(OnlineOrderEntity.class));
        verify(syncEventRepository).save(any(SyncEventEntity.class));
    }

    @Test
    void shouldCreateOnlineOrderWithPaymentPending() {
        // Given
        UUID productId = UUID.randomUUID();
        CustomerRequest customerReq = new CustomerRequest("John Doe", "123456789", "john@example.com", null);
        OnlineOrderItemRequest itemReq = new OnlineOrderItemRequest(productId, BigDecimal.ONE, null);
        CreateOnlineOrderRequest request = new CreateOnlineOrderRequest(
                customerReq,
                DeliveryType.PICKUP,
                null,
                List.of(itemReq),
                null,
                br.com.novaalianca.mnss.core.payment.PaymentMethod.ONLINE_PIX
        );

        OnlineProductEntity product = mock(OnlineProductEntity.class);
        lenient().when(product.getId()).thenReturn(productId);
        lenient().when(product.getName()).thenReturn("Burger");
        lenient().when(product.getPrice()).thenReturn(new BigDecimal("20.00"));
        lenient().when(product.isActive()).thenReturn(true);
        lenient().when(product.isSellOnline()).thenReturn(true);
        lenient().when(product.isAvailable()).thenReturn(true);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(customerRepository.findByPhone(any())).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(orderRepository.save(any())).thenAnswer(i -> (OnlineOrderEntity) i.getArguments()[0]);

        // When
        OnlineOrderResponse response = orderService.createOnlineOrder(request);

        // Then
        assertNotNull(response);
        assertEquals(OrderStatus.PAYMENT_PENDING, response.status());
        assertEquals(br.com.novaalianca.mnss.core.payment.PaymentMethod.ONLINE_PIX, response.paymentMethod());
        
        verify(syncEventRepository, never()).save(any());
    }

    @Test
    void shouldFailIfProductIsUnavailable() {
        // Given
        UUID productId = UUID.randomUUID();
        CustomerRequest customerReq = new CustomerRequest("John Doe", "123456789", "john@example.com", null);
        OnlineOrderItemRequest itemReq = new OnlineOrderItemRequest(productId, BigDecimal.ONE, null);
        CreateOnlineOrderRequest request = new CreateOnlineOrderRequest(
                customerReq,
                DeliveryType.PICKUP,
                null,
                List.of(itemReq),
                null,
                br.com.novaalianca.mnss.core.payment.PaymentMethod.CASH
        );

        OnlineProductEntity product = mock(OnlineProductEntity.class);
        when(product.isActive()).thenReturn(true);
        when(product.isSellOnline()).thenReturn(true);
        when(product.isAvailable()).thenReturn(false); // UNAVAILABLE

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(customerRepository.findByPhone(any())).thenReturn(Optional.of(mock(OnlineCustomerEntity.class)));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> orderService.createOnlineOrder(request));
        verify(orderRepository, never()).save(any());
    }
}
