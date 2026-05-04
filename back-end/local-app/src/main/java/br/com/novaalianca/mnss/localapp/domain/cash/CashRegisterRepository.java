package br.com.novaalianca.mnss.localapp.domain.cash;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashRegisterRepository extends JpaRepository<CashRegisterEntity, UUID> {
    Optional<CashRegisterEntity> findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(
            UUID operatorId,
            CashRegisterStatus status);

    boolean existsByOperatorIdAndStatus(UUID operatorId, CashRegisterStatus status);
}
