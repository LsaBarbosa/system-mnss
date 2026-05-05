package br.com.novaalianca.mnss.localapp.domain.kds;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record KdsTicketResponse(
    UUID id,
    UUID orderId,
    Long ticketNumber,
    PreparationSector sector,
    KdsTicketStatus status,
    Instant createdAt,
    Instant startedAt,
    Instant readyAt,
    List<KdsTicketItemResponse> items
) {}
