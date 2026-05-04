package br.com.novaalianca.mnss.localapp.domain.cash;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashMovementRepository extends JpaRepository<CashMovementEntity, UUID> {
    List<CashMovementEntity> findByCashRegisterIdOrderByCreatedAtAsc(UUID cashRegisterId);
}
