package br.com.novaalianca.mnss.localapp.domain.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductSaleabilityServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private StockBalanceRepository stockBalanceRepository;

    @Test
    void canSell_notStockControlled_doesNotCheckBalance() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = activeProduct(productId, false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        boolean result = service().canSell(productId, SalesChannel.PDV, new BigDecimal("100.000"));

        assertThat(result).isTrue();
    }

    @Test
    void canSell_stockControlled_sufficientBalance_returnsTrue() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = activeProduct(productId, true);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("10.000"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductId(productId)).thenReturn(Optional.of(balance));

        boolean result = service().canSell(productId, SalesChannel.PDV, new BigDecimal("5.000"));

        assertThat(result).isTrue();
    }

    @Test
    void canSell_stockControlled_insufficientBalance_returnsFalse() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = activeProduct(productId, true);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("2.000"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductId(productId)).thenReturn(Optional.of(balance));

        boolean result = service().canSell(productId, SalesChannel.PDV, new BigDecimal("5.000"));

        assertThat(result).isFalse();
    }

    @Test
    void canSell_inactiveProduct_returnsFalse() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = activeProduct(productId, false);
        ReflectionTestUtils.setField(product, "active", false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThat(service().canSell(productId, SalesChannel.PDV, BigDecimal.ONE)).isFalse();
    }

    @Test
    void canSell_productNotFound_returnsFalse() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThat(service().canSell(productId, SalesChannel.PDV, BigDecimal.ONE)).isFalse();
    }

    @Test
    void canSell_stockControlled_noBalanceRecord_returnsFalse() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = activeProduct(productId, true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThat(service().canSell(productId, SalesChannel.PDV, new BigDecimal("1.000"))).isFalse();
    }

    private ProductSaleabilityService service() {
        return new ProductSaleabilityService(productRepository, stockBalanceRepository);
    }

    private ProductEntity activeProduct(UUID id, boolean stockControlled) {
        ProductEntity product = new ProductEntity(
                new CategoryEntity("Paes"),
                "Pao frances",
                new BigDecimal("1.20"),
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO);
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "active", true);
        ReflectionTestUtils.setField(product, "available", true);
        ReflectionTestUtils.setField(product, "sellOnPdv", true);
        ReflectionTestUtils.setField(product, "sellOnline", true);
        ReflectionTestUtils.setField(product, "sellOnWhatsapp", true);
        ReflectionTestUtils.setField(product, "stockControlled", stockControlled);
        return product;
    }
}
