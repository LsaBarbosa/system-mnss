package br.com.novaalianca.mnss.localapp.domain.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
    List<OrderItemEntity> findByOrderIdOrderByCreatedAtAsc(UUID orderId);

    Optional<OrderItemEntity> findByIdAndOrderId(UUID id, UUID orderId);
}
