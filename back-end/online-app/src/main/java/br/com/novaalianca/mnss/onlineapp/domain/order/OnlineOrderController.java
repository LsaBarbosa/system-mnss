package br.com.novaalianca.mnss.onlineapp.domain.order;

import br.com.novaalianca.mnss.onlineapp.domain.order.dto.CreateOnlineOrderRequest;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.OnlineOrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/orders")
public class OnlineOrderController {

    private final OnlineOrderService orderService;

    public OnlineOrderController(OnlineOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OnlineOrderResponse createOnlineOrder(@RequestBody @Valid CreateOnlineOrderRequest request) {
        return orderService.createOnlineOrder(request);
    }
}
