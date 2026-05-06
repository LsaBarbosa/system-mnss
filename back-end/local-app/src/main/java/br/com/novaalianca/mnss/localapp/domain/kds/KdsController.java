package br.com.novaalianca.mnss.localapp.domain.kds;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA', 'ATENDENTE', 'EXPEDICAO')")
    public List<KdsTicketResponse> getTickets(@RequestParam(required = false) PreparationSector sector) {
        return kdsService.getTickets(sector);
    }

    @PatchMapping("/tickets/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA')")
    public KdsTicketResponse start(@PathVariable UUID id, HttpServletRequest request) {
        return kdsService.startTicket(id, authenticatedUserId(request));
    }

    @PatchMapping("/tickets/{id}/ready")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA')")
    public KdsTicketResponse ready(@PathVariable UUID id, HttpServletRequest request) {
        return kdsService.readyTicket(id, authenticatedUserId(request));
    }

    @PatchMapping("/tickets/{id}/finish")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA')")
    public KdsTicketResponse finish(@PathVariable UUID id, HttpServletRequest request) {
        return kdsService.finishTicket(id, authenticatedUserId(request));
    }

    @PatchMapping("/items/{id}/ready")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHA')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readyItem(@PathVariable UUID id, HttpServletRequest request) {
        kdsService.readyItem(id, authenticatedUserId(request));
    }

    @PatchMapping("/orders/{id}/finish")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'EXPEDICAO', 'COZINHA')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishOrder(@PathVariable UUID id, HttpServletRequest request) {
        kdsService.finishOrder(id, authenticatedUserId(request));
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUser.AUTHENTICATED_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser user ? user.id() : null;
    }
}
