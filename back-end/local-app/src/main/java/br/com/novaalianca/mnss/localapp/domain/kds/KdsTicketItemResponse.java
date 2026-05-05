package br.com.novaalianca.mnss.localapp.domain.kds;

import java.util.UUID;

public record KdsTicketItemResponse(
    UUID id,
    String productName,
    Double quantity,
    String observation,
    KdsTicketStatus status
) {}
