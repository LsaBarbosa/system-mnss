package br.com.novaalianca.mnss.onlineapp.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface OnlinePaymentRepository extends JpaRepository<OnlinePaymentEntity, UUID> {
    Optional<OnlinePaymentEntity> findByTransactionId(String transactionId);
    List<OnlinePaymentEntity> findByOrderId(UUID orderId);
}
