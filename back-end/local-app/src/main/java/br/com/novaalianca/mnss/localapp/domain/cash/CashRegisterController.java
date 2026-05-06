package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import org.springframework.security.access.prepost.PreAuthorize;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cash-register")
class CashRegisterController {
    private final CashRegisterService cashRegisterService;

    CashRegisterController(CashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'CAIXA')")
    CashRegisterResponse open(
            @Valid @RequestBody CashRegisterOpenRequest request,
            HttpServletRequest servletRequest) {
        return cashRegisterService.open(request, authenticatedUserId(servletRequest));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'CAIXA')")
    CurrentCashRegisterResponse current(HttpServletRequest servletRequest) {
        return cashRegisterService.current(authenticatedUserId(servletRequest));
    }

    @PostMapping("/{id}/movement")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    CashMovementResponse createMovement(
            @PathVariable UUID id,
            @Valid @RequestBody CashMovementRequest request,
            HttpServletRequest servletRequest) {
        return cashRegisterService.createManualMovement(id, request, authenticatedUserId(servletRequest));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'CAIXA')")
    CashRegisterSummaryResponse close(
            @PathVariable UUID id,
            @Valid @RequestBody CashRegisterCloseRequest request,
            HttpServletRequest servletRequest) {
        return cashRegisterService.close(id, request, authenticatedUserId(servletRequest));
    }

    @GetMapping("/{id}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'CAIXA')")
    CashRegisterSummaryResponse summary(@PathVariable UUID id) {
        return cashRegisterService.summary(id);
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUser.AUTHENTICATED_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser user ? user.id() : null;
    }
}
