package br.com.novaalianca.mnss.localapp.domain.kds;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.localapp.domain.order.OrderOrigin;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record KdsTicketResponse(
    UUID id,
    UUID orderId,
    Long ticketNumber,
    OrderOrigin origin,
    PreparationSector sector,
    KdsTicketStatus status,
    Instant createdAt,
    Instant updatedAt,
    Instant startedAt,
    Instant readyAt,
    Instant finishedAt,
    List<KdsTicketItemResponse> items
) {}
