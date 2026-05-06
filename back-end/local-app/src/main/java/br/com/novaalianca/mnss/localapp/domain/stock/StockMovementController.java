package br.com.novaalianca.mnss.localapp.domain.stock;

import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import org.springframework.security.access.prepost.PreAuthorize;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-movements")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
class StockMovementController {
    private final StockService stockService;

    StockMovementController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    List<StockMovementResponse> listMovements(@RequestParam(required = false) UUID productId) {
        return stockService.listMovements(productId);
    }

    @GetMapping("/balances")
    List<StockBalanceResponse> listBalances() {
        return stockService.listBalances();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    StockMovementResponse createMovement(
            @Valid @RequestBody CreateStockMovementRequest request,
            HttpServletRequest servletRequest) {
        return stockService.createMovement(request, authenticatedUserId(servletRequest));
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUser.AUTHENTICATED_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser user ? user.id() : null;
    }
}
