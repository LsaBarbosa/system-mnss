package br.com.novaalianca.mnss.onlineapp.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OnlineRoleRepository extends JpaRepository<OnlineRoleEntity, UUID> {
    Optional<OnlineRoleEntity> findByName(String name);
}
