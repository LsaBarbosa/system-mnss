package br.com.novaalianca.mnss.localapp.domain.kds;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/kds")
public class KdsController {
    private final KdsService kdsService;

    public KdsController(KdsService kdsService) {
        this.kdsService = kdsService;
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA', 'ATENDENTE')")
    public List<KdsTicketResponse> getTickets(@RequestParam(required = false) PreparationSector sector) {
        return kdsService.getTickets(sector);
    }

    @PatchMapping("/tickets/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA')")
    public KdsTicketResponse start(@PathVariable UUID id) {
        return kdsService.startTicket(id);
    }

    @PatchMapping("/tickets/{id}/ready")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA')")
    public KdsTicketResponse ready(@PathVariable UUID id) {
        return kdsService.readyTicket(id);
    }

    @PatchMapping("/tickets/{id}/finish")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA')")
    public KdsTicketResponse finish(@PathVariable UUID id) {
        return kdsService.finishTicket(id);
    }

    @PatchMapping("/items/{id}/ready")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA')")
    public void readyItem(@PathVariable UUID id) {
        kdsService.readyItem(id);
    }

    @PatchMapping("/orders/{id}/finish")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'EXPEDICAO', 'COZINHA')")
    public void finishOrder(@PathVariable UUID id) {
        kdsService.finishOrder(id);
    }
}
