package br.com.novaalianca.mnss.localapp.domain.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductEntityTest {
    @Test
    void fillsTechnicalTimestampsOnCreation() {
        CategoryEntity category = new CategoryEntity("Paes");

        assertThat(category.getId()).isNull();
        assertThat(category.getCreatedAt()).isNotNull();
        assertThat(category.getUpdatedAt()).isNotNull();
    }

    @Test
    void refusesNegativePrice() {
        CategoryEntity category = new CategoryEntity("Paes");

        assertThatThrownBy(() -> new ProductEntity(
                        category,
                        "Pao frances",
                        new BigDecimal("-0.01"),
                        UnitType.UNIT,
                        PreparationSector.SEM_PREPARO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("price must not be negative");
    }

    @Test
    void refusesNegativeAvailabilityQuantity() {
        CategoryEntity category = new CategoryEntity("Paes");
        ProductEntity product = new ProductEntity(
                category,
                "Pao frances",
                new BigDecimal("1.20"),
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO);
        ProductAvailabilityEntity availability = new ProductAvailabilityEntity(
                product,
                AvailabilityStatus.AVAILABLE,
                SalesChannel.PDV);

        assertThatThrownBy(() -> availability.setAvailableQuantity(new BigDecimal("-1.000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("availableQuantity must not be negative");
    }
}
