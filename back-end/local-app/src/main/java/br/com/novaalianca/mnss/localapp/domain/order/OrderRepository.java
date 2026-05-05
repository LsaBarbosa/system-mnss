package br.com.novaalianca.mnss.localapp.domain.order;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByOriginOrderByCreatedAtDesc(OrderOrigin origin);
}
