package br.com.novaalianca.mnss.localapp.domain.hardware;

import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemEntity;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HardwareAdapterService {
    private static final Logger log = LoggerFactory.getLogger(HardwareAdapterService.class);

    public void printReceipt(OrderEntity order, List<OrderItemEntity> items, List<PaymentEntity> payments) {
        // Simulated ESC/POS printing
        log.info("--- SIMULANDO IMPRESSAO DE COMPROVANTE ---");
        log.info("Pedido: {}", order.getOrderNumber());
        log.info("Itens:");
        for (OrderItemEntity item : items) {
            log.info("  {} x{} - {}", item.getProductNameSnapshot(), item.getQuantity(), item.getTotalPrice());
        }
        log.info("Desconto: {}", order.getDiscountAmount());
        log.info("Total: {}", order.getTotalAmount());
        log.info("Pagamentos:");
        for (PaymentEntity payment : payments) {
            log.info("  {} - {}", payment.getMethod(), payment.getAmount());
        }
        log.info("------------------------------------------");
    }

    public void openDrawer() {
        // Simulated ESC/POS open drawer command
        log.info("--- SIMULANDO ABERTURA DE GAVETA ---");
    }
}
