package br.com.novaalianca.mnss.localapp.domain.payment;

import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import org.springframework.security.access.prepost.PreAuthorize;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/{orderId}/payments")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'CAIXA', 'ATENDENTE')")
class PaymentController {
    private final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PaymentResponse payOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest servletRequest) {
        return paymentService.payOrder(orderId, request, authenticatedUserId(servletRequest));
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUser.AUTHENTICATED_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser user ? user.id() : null;
    }
}
