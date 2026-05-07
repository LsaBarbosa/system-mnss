package br.com.novaalianca.mnss.localapp.domain.kds;

import java.math.BigDecimal;
import java.util.UUID;

public record KdsTicketItemResponse(
    UUID id,
    String productName,
    BigDecimal quantity,
    String observation,
    KdsTicketStatus status
) {}
