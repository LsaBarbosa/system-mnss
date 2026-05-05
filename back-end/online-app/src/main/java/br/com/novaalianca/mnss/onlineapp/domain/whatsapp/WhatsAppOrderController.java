package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.onlineapp.domain.order.DeliveryType;
import br.com.novaalianca.mnss.onlineapp.domain.order.OnlineOrderService;
import br.com.novaalianca.mnss.onlineapp.domain.order.OrderOrigin;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.CreateOnlineOrderRequest;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.CustomerRequest;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.OnlineOrderItemRequest;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.OnlineOrderResponse;
import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto.ConversationResponse;
import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto.CreateWhatsAppOrderRequest;
import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto.MessageResponse;
import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Authenticated controller for attendants managing WhatsApp conversations and assisted orders.
 */
@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppOrderController {

    private final WhatsAppService whatsAppService;
    private final OnlineOrderService orderService;

    public WhatsAppOrderController(WhatsAppService whatsAppService, OnlineOrderService orderService) {
        this.whatsAppService = whatsAppService;
        this.orderService = orderService;
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> listConversations() {
        return whatsAppService.listConversations();
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageResponse> getMessages(@PathVariable UUID id) {
        return whatsAppService.getConversationMessages(id);
    }

    @PostMapping("/conversations/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(@PathVariable UUID id, @Valid @RequestBody SendMessageRequest request) {
        return whatsAppService.sendMessage(id, request.content());
    }

    @PostMapping("/conversations/{id}/assign")
    public ConversationResponse assignConversation(@PathVariable UUID id, @RequestBody java.util.Map<String, UUID> body) {
        return whatsAppService.assignConversation(id, body.get("userId"));
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OnlineOrderResponse createAssistedOrder(@Valid @RequestBody CreateWhatsAppOrderRequest request) {
        // Build the standard order request from the WhatsApp-specific request
        CustomerRequest customerRequest = new CustomerRequest(
                request.customerName(),
                request.customerPhone(),
                request.customerEmail(),
                null);

        List<OnlineOrderItemRequest> items = request.items().stream()
                .map(item -> new OnlineOrderItemRequest(
                        item.productId(),
                        item.quantity() != null ? item.quantity() : BigDecimal.ONE,
                        item.observation()))
                .toList();

        // WhatsApp orders default to CASH payment (pay on pickup/delivery)
        CreateOnlineOrderRequest orderRequest = new CreateOnlineOrderRequest(
                customerRequest,
                request.deliveryType(),
                null, // No address for now — pickup by default
                items,
                request.notes(),
                PaymentMethod.CASH);

        OnlineOrderResponse response = orderService.createOnlineOrder(orderRequest, OrderOrigin.WHATSAPP);

        // Link order to conversation
        whatsAppService.linkOrderToConversation(request.conversationId(), response.id());

        // Send order summary to customer
        StringBuilder summary = new StringBuilder();
        summary.append("📋 *Resumo do pedido:*\n");
        for (var item : request.items()) {
            summary.append("• ").append(item.productId().toString(), 0, 8).append(" x")
                    .append(item.quantity() != null ? item.quantity() : 1).append("\n");
        }
        summary.append("\n💰 *Total:* R$ ").append(response.totalAmount());

        try {
            whatsAppService.sendOrderSummary(request.conversationId(), response.id(), summary.toString());
        } catch (Exception e) {
            // Provider failure must not cancel the order (S20-H04 requirement)
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .error("Failed to send order summary via WhatsApp: {}", e.getMessage());
        }

        return response;
    }
}
