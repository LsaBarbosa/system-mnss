package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import org.springframework.security.access.prepost.PreAuthorize;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import br.com.novaalianca.mnss.localapp.domain.payment.CreatePaymentRequest;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentResponse;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentService;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'CAIXA', 'ATENDENTE')")
class PdvSaleController {
    private final PdvSaleService pdvSaleService;
    private final PaymentService paymentService;

    PdvSaleController(PdvSaleService pdvSaleService, PaymentService paymentService) {
        this.pdvSaleService = pdvSaleService;
        this.paymentService = paymentService;
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

    @PostMapping("/{saleId}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    PaymentResponse pay(
            @PathVariable UUID saleId,
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest servletRequest) {
        return paymentService.payOrder(saleId, request, authenticatedUserId(servletRequest));
    }

    @PostMapping("/{saleId}/finish")
    PdvSaleResponse finishSale(@PathVariable UUID saleId, HttpServletRequest servletRequest) {
        return pdvSaleService.finishSale(saleId, authenticatedUserId(servletRequest));
    }

    @PostMapping("/{saleId}/discount")
    PdvSaleResponse applyDiscount(
            @PathVariable UUID saleId,
            @Valid @RequestBody CreateDiscountRequest request,
            HttpServletRequest servletRequest) {
        return pdvSaleService.applyDiscount(saleId, request, authenticatedUserId(servletRequest), authenticatedUserRoles(servletRequest));
    }

    @PostMapping("/{saleId}/cancel")
    PdvSaleResponse cancelSale(
            @PathVariable UUID saleId,
            @Valid @RequestBody CancelSaleRequest request,
            HttpServletRequest servletRequest) {
        return pdvSaleService.cancelSale(saleId, request, authenticatedUserId(servletRequest));
    }

    @PostMapping("/{saleId}/print")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reprint(@PathVariable UUID saleId) {
        pdvSaleService.reprintReceipt(saleId);
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUser.AUTHENTICATED_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser user ? user.id() : null;
    }

    private List<String> authenticatedUserRoles(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUser.AUTHENTICATED_USER_ATTRIBUTE);
        if (attribute instanceof AuthenticatedUser user) {
            return user.roles().stream().map(RoleName::name).collect(Collectors.toList());
        }
        return List.of();
    }
}
