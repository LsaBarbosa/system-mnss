package br.com.novaalianca.mnss.localapp.security.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(String name);

    List<RoleEntity> findByNameIn(Collection<String> names);
}
