package br.com.novaalianca.mnss.localapp.domain.catalog;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findAllByOrderByDisplayOrderAscNameAsc();

    List<CategoryEntity> findByActiveTrueAndShowOnPdvTrueOrderByDisplayOrderAscNameAsc();
}
