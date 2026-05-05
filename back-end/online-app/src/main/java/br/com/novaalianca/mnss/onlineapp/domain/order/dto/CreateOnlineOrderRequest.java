package br.com.novaalianca.mnss.onlineapp.domain.order.dto;

import br.com.novaalianca.mnss.onlineapp.domain.order.DeliveryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOnlineOrderRequest(
        @Valid
        @NotNull(message = "Os dados do cliente são obrigatórios")
        CustomerRequest customer,

        @NotNull(message = "O tipo de entrega é obrigatório")
        DeliveryType deliveryType,

        @Valid
        AddressRequest address,

        @Valid
        @NotEmpty(message = "O pedido deve ter pelo menos um item")
        List<OnlineOrderItemRequest> items,

        String notes,

        @NotNull(message = "A forma de pagamento é obrigatória")
        br.com.novaalianca.mnss.core.payment.PaymentMethod paymentMethod
) {}
