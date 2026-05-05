package br.com.novaalianca.mnss.localapp.domain.kds;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemStatus;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KdsServiceTest {
    @Mock
    private KdsTicketRepository ticketRepository;
    @Mock
    private KdsTicketItemRepository itemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private KdsService kdsService;

    @Test
    void createTicketsForOrderGroupsBySector() {
        OrderEntity order = mock(OrderEntity.class);
        when(order.getId()).thenReturn(UUID.randomUUID());
        when(ticketRepository.save(any(KdsTicketEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(itemRepository.save(any(KdsTicketItemEntity.class))).thenAnswer(i -> i.getArgument(0));

        OrderItemEntity item1 = new OrderItemEntity(order, null, "Burger", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, OrderItemStatus.CREATED, PreparationSector.CHAPA);
        OrderItemEntity item2 = new OrderItemEntity(order, null, "Soda", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, OrderItemStatus.CREATED, PreparationSector.BEBIDAS);
        OrderItemEntity item3 = new OrderItemEntity(order, null, "Chips", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, OrderItemStatus.CREATED, PreparationSector.SEM_PREPARO);

        kdsService.createTicketsForOrder(order, List.of(item1, item2, item3));

        verify(ticketRepository, times(2)).save(any(KdsTicketEntity.class)); // CHAPA and BEBIDAS
        verify(itemRepository, times(2)).save(any(KdsTicketItemEntity.class));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), any(KdsTicketResponse.class));
    }

    @Test
    void shouldMarkOrderAsReadyWhenAllTicketsAreReady() {
        OrderEntity order = mock(OrderEntity.class);
        UUID orderId = UUID.randomUUID();
        when(order.getId()).thenReturn(orderId);
        KdsTicketEntity ticket = new KdsTicketEntity(order, PreparationSector.CHAPA, KdsTicketStatus.IN_PREPARATION);
        
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ticketRepository.findByOrder(order)).thenReturn(List.of(ticket));

        kdsService.readyTicket(UUID.randomUUID());

        verify(ticketRepository).save(ticket);
        assert ticket.getStatus() == KdsTicketStatus.READY;
        verify(messagingTemplate).convertAndSend(eq("/topic/orders/ready"), any(UUID.class));
    }
}
