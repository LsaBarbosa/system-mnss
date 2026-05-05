package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnlineCategoryRepository extends JpaRepository<OnlineCategoryEntity, UUID> {
    List<OnlineCategoryEntity> findAllByOrderByDisplayOrderAscNameAsc();
}
