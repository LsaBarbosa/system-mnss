package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.onlineapp.domain.store.StoreInfoProperties;
import br.com.novaalianca.mnss.onlineapp.domain.store.StoreInfoResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PublicMenuServiceTest {

    @Mock
    private OnlineCategoryRepository categoryRepository;

    @Mock
    private OnlineProductRepository productRepository;

    @Mock
    private OnlineProductAvailabilityRepository productAvailabilityRepository;

    @Mock
    private StoreInfoProperties storeInfoProperties;

    @InjectMocks
    private PublicMenuService service;

    private OnlineCategoryEntity activeCategory;
    private OnlineCategoryEntity inactiveCategory;
    private OnlineProductEntity activeProduct;
    private OnlineProductEntity inactiveProduct;
    private OnlineProductEntity notSellOnlineProduct;

    @BeforeEach
    void setUp() {
        activeCategory = new OnlineCategoryEntity();
        ReflectionTestUtils.setField(activeCategory, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(activeCategory, "name", "Category 1");
        ReflectionTestUtils.setField(activeCategory, "active", true);
        ReflectionTestUtils.setField(activeCategory, "showOnline", true);

        inactiveCategory = new OnlineCategoryEntity();
        ReflectionTestUtils.setField(inactiveCategory, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(inactiveCategory, "active", false);

        activeProduct = new OnlineProductEntity();
        ReflectionTestUtils.setField(activeProduct, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(activeProduct, "name", "Product 1");
        ReflectionTestUtils.setField(activeProduct, "active", true);
        ReflectionTestUtils.setField(activeProduct, "available", true);
        ReflectionTestUtils.setField(activeProduct, "sellOnline", true);
        ReflectionTestUtils.setField(activeProduct, "category", activeCategory);

        inactiveProduct = new OnlineProductEntity();
        ReflectionTestUtils.setField(inactiveProduct, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(inactiveProduct, "active", false);

        notSellOnlineProduct = new OnlineProductEntity();
        ReflectionTestUtils.setField(notSellOnlineProduct, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(notSellOnlineProduct, "active", true);
        ReflectionTestUtils.setField(notSellOnlineProduct, "available", true);
        ReflectionTestUtils.setField(notSellOnlineProduct, "sellOnline", false);
        ReflectionTestUtils.setField(notSellOnlineProduct, "category", activeCategory);
    }

    @Test
    void shouldReturnOnlyActiveCategoriesAndProducts() {
        when(categoryRepository.findAllByOrderByDisplayOrderAscNameAsc())
                .thenReturn(List.of(activeCategory, inactiveCategory));
        when(productRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(activeProduct, inactiveProduct, notSellOnlineProduct));

        List<PublicMenuResponse> menu = service.listMenu(null);

        assertThat(menu).hasSize(1);
        assertThat(menu.getFirst().category().name()).isEqualTo("Category 1");
        assertThat(menu.getFirst().products()).hasSize(1);
        assertThat(menu.getFirst().products().getFirst().name()).isEqualTo("Product 1");
    }

    @Test
    void shouldFilterBySearchTerm() {
        when(categoryRepository.findAllByOrderByDisplayOrderAscNameAsc())
                .thenReturn(List.of(activeCategory));
        when(productRepository.findByNameContainingIgnoreCaseOrderByNameAsc("Product 1"))
                .thenReturn(List.of(activeProduct));

        List<PublicMenuResponse> menu = service.listMenu("Product 1");

        assertThat(menu).hasSize(1);
        assertThat(menu.getFirst().products()).hasSize(1);
        assertThat(menu.getFirst().products().getFirst().name()).isEqualTo("Product 1");
    }

    @Test
    void shouldExcludeUnavailableProducts() {
        OnlineProductAvailabilityEntity availability = new OnlineProductAvailabilityEntity();
        ReflectionTestUtils.setField(availability, "status", "UNAVAILABLE");

        when(categoryRepository.findAllByOrderByDisplayOrderAscNameAsc())
                .thenReturn(List.of(activeCategory));
        when(productRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(activeProduct));
        when(productAvailabilityRepository.findFirstByProductIdAndChannelOrderByUpdatedAtDesc(
                        activeProduct.getId(), SalesChannel.ALL))
                .thenReturn(Optional.of(availability));

        List<PublicMenuResponse> menu = service.listMenu(null);

        assertThat(menu).isEmpty();
    }

    @Test
    void shouldGetStoreInfo() {
        when(storeInfoProperties.getName()).thenReturn("Padaria");
        when(storeInfoProperties.getAddress()).thenReturn("Rua 1");

        StoreInfoResponse info = service.getStoreInfo();

        assertThat(info.name()).isEqualTo("Padaria");
        assertThat(info.address()).isEqualTo("Rua 1");
    }
}
