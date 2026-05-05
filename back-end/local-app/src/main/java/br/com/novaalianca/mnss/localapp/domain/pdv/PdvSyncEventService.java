package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventService;
import br.com.novaalianca.mnss.sync.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import java.util.Optional;

@Service
public class PdvSyncEventService {
    private final Optional<SyncEventService> syncEventService;
 
     public PdvSyncEventService(Optional<SyncEventService> syncEventService) {
         this.syncEventService = syncEventService;
     }

    @Transactional
    public void recordOrderFinishedEvent(OrderEntity order) {
        syncEventService.ifPresent(service -> 
            service.createPending(
                "Order",
                order.getId(),
                "SALE_FINISHED",
                Map.of("orderId", order.getId().toString())
            )
        );
    }
}
