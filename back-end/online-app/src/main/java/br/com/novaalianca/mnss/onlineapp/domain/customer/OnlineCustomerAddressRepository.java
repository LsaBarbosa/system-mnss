package br.com.novaalianca.mnss.onlineapp.domain.customer;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnlineCustomerAddressRepository extends JpaRepository<OnlineCustomerAddressEntity, UUID> {
    List<OnlineCustomerAddressEntity> findByCustomerId(UUID customerId);
}
