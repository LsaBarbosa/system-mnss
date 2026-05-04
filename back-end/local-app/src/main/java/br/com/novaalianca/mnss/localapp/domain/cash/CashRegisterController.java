package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUserInterceptor;
import br.com.novaalianca.mnss.localapp.security.auth.RequiresRole;
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
    @RequiresRole({RoleName.ADMIN, RoleName.GERENTE, RoleName.CAIXA})
    CashRegisterResponse open(
            @Valid @RequestBody CashRegisterOpenRequest request,
            HttpServletRequest servletRequest) {
        return cashRegisterService.open(request, authenticatedUserId(servletRequest));
    }

    @GetMapping("/current")
    @RequiresRole({RoleName.ADMIN, RoleName.GERENTE, RoleName.CAIXA})
    CurrentCashRegisterResponse current(HttpServletRequest servletRequest) {
        return cashRegisterService.current(authenticatedUserId(servletRequest));
    }

    @PostMapping("/{id}/movement")
    @RequiresRole({RoleName.ADMIN, RoleName.GERENTE})
    CashMovementResponse createMovement(
            @PathVariable UUID id,
            @Valid @RequestBody CashMovementRequest request,
            HttpServletRequest servletRequest) {
        return cashRegisterService.createManualMovement(id, request, authenticatedUserId(servletRequest));
    }

    @PostMapping("/{id}/close")
    @RequiresRole({RoleName.ADMIN, RoleName.GERENTE, RoleName.CAIXA})
    CashRegisterSummaryResponse close(
            @PathVariable UUID id,
            @Valid @RequestBody CashRegisterCloseRequest request,
            HttpServletRequest servletRequest) {
        return cashRegisterService.close(id, request, authenticatedUserId(servletRequest));
    }

    @GetMapping("/{id}/summary")
    @RequiresRole({RoleName.ADMIN, RoleName.GERENTE, RoleName.CAIXA})
    CashRegisterSummaryResponse summary(@PathVariable UUID id) {
        return cashRegisterService.summary(id);
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUserInterceptor.AUTHENTICATED_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser user ? user.id() : null;
    }
}
