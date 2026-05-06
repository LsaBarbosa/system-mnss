package br.com.novaalianca.mnss.localapp.domain.kds;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KdsService {
    private static final Logger log = LoggerFactory.getLogger(KdsService.class);

    private final KdsTicketRepository ticketRepository;
    private final KdsTicketItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditService auditService;

    public KdsService(
            KdsTicketRepository ticketRepository,
            KdsTicketItemRepository itemRepository,
            OrderRepository orderRepository,
            SimpMessagingTemplate messagingTemplate,
            AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.messagingTemplate = messagingTemplate;
        this.auditService = auditService;
    }

    @Transactional
    public void createTicketsForOrder(OrderEntity order, List<OrderItemEntity> items) {
        Map<PreparationSector, List<OrderItemEntity>> itemsBySector = items.stream()
                .filter(item -> item.getPreparationSector() != PreparationSector.SEM_PREPARO)
                .collect(Collectors.groupingBy(OrderItemEntity::getPreparationSector));

        itemsBySector.forEach((sector, sectorItems) -> {
            KdsTicketEntity ticket = new KdsTicketEntity(order, sector, KdsTicketStatus.WAITING);
            ticketRepository.save(ticket);

            List<KdsTicketItemEntity> ticketItems = sectorItems.stream()
                    .map(item -> {
                        KdsTicketItemEntity ticketItem = new KdsTicketItemEntity(ticket, item, KdsTicketStatus.WAITING);
                        return itemRepository.save(ticketItem);
                    })
                    .collect(Collectors.toList());

            notifyTicketCreated(ticket, ticketItems);
        });
    }

    @Transactional
    public KdsTicketResponse startTicket(UUID id, UUID actorUserId) {
        KdsTicketEntity ticket = findTicket(id);
        if (ticket.getStatus() != KdsTicketStatus.WAITING) {
            throw new br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException("INVALID_STATUS", "Only WAITING tickets can be started", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        ticket.start();
        KdsTicketEntity saved = ticketRepository.save(ticket);

        auditService.record(new AuditLogRequest(
                actorUserId,
                "KDS_TICKET_STARTED",
                "KdsTicket",
                id,
                new LinkedHashMap<>(Map.of(
                        "ticketNumber", saved.getTicketNumber(),
                        "sector", saved.getSector().name(),
                        "status", saved.getStatus().name()
                )),
                null));

        notifyTicketUpdated(saved);
        return mapToResponse(saved);
    }

    @Transactional
    public KdsTicketResponse readyTicket(UUID id, UUID actorUserId) {
        KdsTicketEntity ticket = findTicket(id);
        ticket.ready();
        // Also mark all items as ready
        ticket.getItems().forEach(item -> {
            if (item.getStatus() != KdsTicketStatus.READY && item.getStatus() != KdsTicketStatus.CANCELED) {
                item.ready();
                itemRepository.save(item);
            }
        });
        KdsTicketEntity saved = ticketRepository.save(ticket);

        auditService.record(new AuditLogRequest(
                actorUserId,
                "KDS_TICKET_READY",
                "KdsTicket",
                id,
                new LinkedHashMap<>(Map.of(
                        "ticketNumber", saved.getTicketNumber(),
                        "sector", saved.getSector().name(),
                        "status", saved.getStatus().name()
                )),
                null));

        checkAndUpdateOrderStatus(saved.getOrder());
        notifyTicketUpdated(saved);
        return mapToResponse(saved);
    }

    @Transactional
    public KdsTicketResponse finishTicket(UUID id, UUID actorUserId) {
        KdsTicketEntity ticket = findTicket(id);
        ticket.finish();
        KdsTicketEntity saved = ticketRepository.save(ticket);

        auditService.record(new AuditLogRequest(
                actorUserId,
                "KDS_TICKET_FINISHED",
                "KdsTicket",
                id,
                new LinkedHashMap<>(Map.of(
                        "ticketNumber", saved.getTicketNumber(),
                        "sector", saved.getSector().name(),
                        "status", saved.getStatus().name()
                )),
                null));

        notifyTicketUpdated(saved);
        return mapToResponse(saved);
    }

    @Transactional
    public void readyItem(UUID itemId, UUID actorUserId) {
        KdsTicketItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException("ITEM_NOT_FOUND", "Item not found", org.springframework.http.HttpStatus.NOT_FOUND));

        if (item.getStatus() == KdsTicketStatus.CANCELED) {
            throw new br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException("INVALID_STATUS", "Canceled items cannot be ready", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        item.ready();
        itemRepository.save(item);

        auditService.record(new AuditLogRequest(
                actorUserId,
                "KDS_ITEM_READY",
                "KdsTicketItem",
                itemId,
                new LinkedHashMap<>(Map.of(
                        "status", item.getStatus().name()
                )),
                null));

        // Check if all items in the ticket are ready
        KdsTicketEntity ticket = item.getKdsTicket();
        boolean allReady = ticket.getItems().stream()
                .allMatch(i -> i.getStatus() == KdsTicketStatus.READY || i.getStatus() == KdsTicketStatus.CANCELED);

        if (allReady) {
            ticket.ready();
            ticketRepository.save(ticket);
            checkAndUpdateOrderStatus(ticket.getOrder());
        }

        notifyTicketUpdated(ticket);
    }

    private void checkAndUpdateOrderStatus(OrderEntity order) {
        List<KdsTicketEntity> orderTickets = ticketRepository.findByOrder(order);
        boolean allTicketsReady = orderTickets.stream()
                .allMatch(t -> t.getStatus() == KdsTicketStatus.READY || t.getStatus() == KdsTicketStatus.FINISHED || t.getStatus() == KdsTicketStatus.CANCELED);
        
        if (allTicketsReady && !orderTickets.isEmpty()) {
            order.markAsReady();
            notifyOrderReady(order);
        }
    }

    private void notifyOrderReady(OrderEntity order) {
        try {
            messagingTemplate.convertAndSend("/topic/orders/ready", order.getId());
        } catch (Exception e) {
            log.warn("Failed to send order ready notification via WebSocket for order {}: {}", order.getId(), e.getMessage(), e);
        }
    }

    private KdsTicketEntity findTicket(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException("KDS_TICKET_NOT_FOUND", "Ticket not found", org.springframework.http.HttpStatus.NOT_FOUND));
    }

    private void notifyTicketUpdated(KdsTicketEntity ticket) {
        try {
            KdsTicketResponse response = mapToResponse(ticket);
            messagingTemplate.convertAndSend("/topic/kds/tickets", response);
            messagingTemplate.convertAndSend("/topic/kds/tickets/" + ticket.getSector(), response);
        } catch (Exception e) {
            log.warn("Failed to send KDS ticket update via WebSocket for ticket {}: {}", ticket.getId(), e.getMessage(), e);
        }
    }

    @Transactional
    public void finishOrder(UUID orderId, UUID actorUserId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException("ORDER_NOT_FOUND", "Order not found", org.springframework.http.HttpStatus.NOT_FOUND));

        if (order.getStatus() != OrderStatus.READY) {
            throw new br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException("INVALID_STATUS", "Only READY orders can be finished", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        order.finish();

        auditService.record(new AuditLogRequest(
                actorUserId,
                "KDS_ORDER_FINISHED",
                "Order",
                orderId,
                new LinkedHashMap<>(Map.of(
                        "status", order.getStatus().name()
                )),
                null));

        List<KdsTicketEntity> tickets = ticketRepository.findByOrder(order);
        tickets.forEach(t -> {
            t.finish();
            ticketRepository.save(t);
            notifyTicketUpdated(t);
        });
    }

    @Transactional(readOnly = true)
    public List<KdsTicketResponse> getTickets(PreparationSector sector) {
        List<KdsTicketEntity> tickets;
        if (sector != null) {
            tickets = ticketRepository.findBySectorOrderByCreatedAtAsc(sector);
        } else {
            tickets = ticketRepository.findAllByOrderByCreatedAtAsc();
        }
        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void notifyTicketCreated(KdsTicketEntity ticket, List<KdsTicketItemEntity> items) {
        try {
            KdsTicketResponse response = mapToResponse(ticket, items);
            messagingTemplate.convertAndSend("/topic/kds/tickets", response);
            messagingTemplate.convertAndSend("/topic/kds/tickets/" + ticket.getSector(), response);
        } catch (Exception e) {
            // WebSocket errors should not break the transaction (S11-H03)
        }
    }

    private KdsTicketResponse mapToResponse(KdsTicketEntity ticket) {
        return mapToResponse(ticket, List.copyOf(ticket.getItems()));
    }

    private KdsTicketResponse mapToResponse(KdsTicketEntity ticket, List<KdsTicketItemEntity> items) {
        List<KdsTicketItemResponse> itemResponses = items.stream()
                .map(item -> new KdsTicketItemResponse(
                        item.getId(),
                        item.getOrderItem().getProductNameSnapshot(),
                        item.getOrderItem().getQuantity(),
                        item.getOrderItem().getObservation(),
                        item.getStatus()
                ))
                .collect(Collectors.toList());

        return new KdsTicketResponse(
                ticket.getId(),
                ticket.getOrder().getId(),
                ticket.getTicketNumber(),
                ticket.getOrder().getOrigin(),
                ticket.getSector(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getStartedAt(),
                ticket.getReadyAt(),
                ticket.getFinishedAt(),
                itemResponses
        );
    }
}
