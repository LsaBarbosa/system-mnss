package br.com.novaalianca.mnss.localapp.domain.stock;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductSaleabilityService {

    private final ProductRepository productRepository;
    private final StockBalanceRepository stockBalanceRepository;

    ProductSaleabilityService(ProductRepository productRepository,
                               StockBalanceRepository stockBalanceRepository) {
        this.productRepository = productRepository;
        this.stockBalanceRepository = stockBalanceRepository;
    }

    @Transactional(readOnly = true)
    public boolean canSell(UUID productId, SalesChannel channel, BigDecimal requestedQuantity) {
        Optional<ProductEntity> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return false;

        ProductEntity product = productOpt.get();
        if (!product.isActive() || !product.isAvailable()) return false;

        if (!isChannelEnabled(product, channel)) return false;

        if (!product.isStockControlled()) return true;

        BigDecimal available = stockBalanceRepository.findByProductId(productId)
                .map(StockBalanceEntity::getAvailableQuantity)
                .orElse(BigDecimal.ZERO);

        return available.compareTo(requestedQuantity) >= 0;
    }

    private boolean isChannelEnabled(ProductEntity product, SalesChannel channel) {
        return switch (channel) {
            case PDV -> product.isSellOnPdv();
            case SITE -> product.isSellOnline();
            case WHATSAPP -> product.isSellOnWhatsapp();
            case ALL -> product.isSellOnPdv() || product.isSellOnline() || product.isSellOnWhatsapp();
        };
    }
}
