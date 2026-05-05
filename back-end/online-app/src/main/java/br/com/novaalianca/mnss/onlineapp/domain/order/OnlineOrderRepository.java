package br.com.novaalianca.mnss.onlineapp.domain.order;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnlineOrderRepository extends JpaRepository<OnlineOrderEntity, UUID> {
}
