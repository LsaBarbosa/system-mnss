package br.com.novaalianca.mnss.localapp.domain.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findAllByOrderByNameAsc();

    List<ProductEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<ProductEntity> findByCategoryIdOrderByNameAsc(UUID categoryId);

    List<ProductEntity> findByCategoryIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID categoryId, String name);

    Optional<ProductEntity> findByBarcode(String barcode);

    Optional<ProductEntity> findByBarcodeAndActiveTrueAndAvailableTrueAndSellOnPdvTrue(String barcode);
}
