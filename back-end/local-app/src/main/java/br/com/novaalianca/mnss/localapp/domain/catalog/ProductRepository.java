package br.com.novaalianca.mnss.localapp.domain.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findAllByOrderByNameAsc(Pageable pageable);

    List<ProductEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String name, Pageable pageable);

    List<ProductEntity> findByCategoryIdOrderByNameAsc(UUID categoryId, Pageable pageable);

    List<ProductEntity> findByCategoryIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID categoryId, String name, Pageable pageable);

    Optional<ProductEntity> findByBarcode(String barcode);

    Optional<ProductEntity> findByBarcodeAndActiveTrueAndAvailableTrueAndSellOnPdvTrue(String barcode);
}
