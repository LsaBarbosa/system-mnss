package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUserInterceptor;
import br.com.novaalianca.mnss.localapp.security.auth.RequiresRole;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdv/sales")
@RequiresRole({RoleName.ADMIN, RoleName.GERENTE, RoleName.CAIXA, RoleName.ATENDENTE})
class PdvSaleController {
    private final PdvSaleService pdvSaleService;

    PdvSaleController(PdvSaleService pdvSaleService) {
        this.pdvSaleService = pdvSaleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PdvSaleResponse createSale(HttpServletRequest servletRequest) {
        return pdvSaleService.createSale(authenticatedUserId(servletRequest));
    }

    @GetMapping("/{saleId}")
    PdvSaleResponse getSale(@PathVariable UUID saleId) {
        return pdvSaleService.getSale(saleId);
    }

    @GetMapping
    List<PdvSaleResponse> listSales() {
        return pdvSaleService.listSales();
    }

    @PostMapping("/{saleId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    PdvSaleResponse addItem(
            @PathVariable UUID saleId,
            @Valid @RequestBody CreatePdvSaleItemRequest request) {
        return pdvSaleService.addItem(saleId, request);
    }

    @PatchMapping("/{saleId}/items/{itemId}")
    PdvSaleResponse updateItem(
            @PathVariable UUID saleId,
            @PathVariable UUID itemId,
            @Valid @RequestBody PatchPdvSaleItemRequest request) {
        return pdvSaleService.updateItem(saleId, itemId, request);
    }

    @DeleteMapping("/{saleId}/items/{itemId}")
    PdvSaleResponse removeItem(
            @PathVariable UUID saleId,
            @PathVariable UUID itemId) {
        return pdvSaleService.removeItem(saleId, itemId);
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUserInterceptor.AUTHENTICATED_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser user ? user.id() : null;
    }
}
