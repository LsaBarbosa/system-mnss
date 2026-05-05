package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.onlineapp.domain.store.StoreInfoProperties;
import br.com.novaalianca.mnss.onlineapp.domain.store.StoreInfoResponse;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicMenuService {
    private final OnlineCategoryRepository categoryRepository;
    private final OnlineProductRepository productRepository;
    private final OnlineProductAvailabilityRepository productAvailabilityRepository;
    private final StoreInfoProperties storeInfoProperties;

    public PublicMenuService(
            OnlineCategoryRepository categoryRepository,
            OnlineProductRepository productRepository,
            OnlineProductAvailabilityRepository productAvailabilityRepository,
            StoreInfoProperties storeInfoProperties) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productAvailabilityRepository = productAvailabilityRepository;
        this.storeInfoProperties = storeInfoProperties;
    }

    @Transactional(readOnly = true)
    public List<PublicMenuResponse> listMenu(String search) {
        List<OnlineCategoryEntity> categories = categoryRepository.findAllByOrderByDisplayOrderAscNameAsc().stream()
                .filter(OnlineCategoryEntity::isActive)
                .filter(OnlineCategoryEntity::isShowOnline)
                .toList();

        List<OnlineProductEntity> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCaseOrderByNameAsc(search);
        } else {
            products = productRepository.findAllByOrderByNameAsc();
        }

        Map<UUID, List<PublicProductResponse>> productsByCategory = products.stream()
                .filter(this::isProductVisible)
                .collect(Collectors.groupingBy(
                        product -> product.getCategory().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(PublicProductResponse::from, Collectors.toList())));

        return categories.stream()
                .map(category -> new PublicMenuResponse(
                        PublicCategoryResponse.from(category),
                        productsByCategory.getOrDefault(category.getId(), List.of()).stream()
                                .sorted(Comparator.comparing(PublicProductResponse::name))
                                .toList()))
                .filter(categoryProducts -> !categoryProducts.products().isEmpty())
                .toList();
    }

    public StoreInfoResponse getStoreInfo() {
        return new StoreInfoResponse(
                storeInfoProperties.getName(),
                storeInfoProperties.getAddress(),
                storeInfoProperties.getHours(),
                storeInfoProperties.getPhone(),
                storeInfoProperties.getDescription());
    }

    private boolean isProductVisible(OnlineProductEntity product) {
        if (!product.isActive() || product.getCategory() == null) {
            return false;
        }
        OnlineCategoryEntity category = product.getCategory();
        if (!category.isActive() || !category.isShowOnline()) {
            return false;
        }
        return product.isSellOnline() && product.isAvailable() && !hasUnavailableStatus(product, SalesChannel.SITE);
    }

    private boolean hasUnavailableStatus(OnlineProductEntity product, SalesChannel channel) {
        return productAvailabilityRepository
                        .findFirstByProductIdAndChannelOrderByUpdatedAtDesc(product.getId(), SalesChannel.ALL)
                        .filter(availability -> "UNAVAILABLE".equals(availability.getStatus()))
                        .isPresent()
                || productAvailabilityRepository
                        .findFirstByProductIdAndChannelOrderByUpdatedAtDesc(product.getId(), channel)
                        .filter(availability -> "UNAVAILABLE".equals(availability.getStatus()))
                        .isPresent();
    }
}
