package br.com.novaalianca.mnss.onlineapp.domain.customer;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnlineCustomerRepository extends JpaRepository<OnlineCustomerEntity, UUID> {
    Optional<OnlineCustomerEntity> findByPhone(String phone);
    Optional<OnlineCustomerEntity> findByEmail(String email);
}
