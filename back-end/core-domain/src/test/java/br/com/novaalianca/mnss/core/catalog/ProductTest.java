package br.com.novaalianca.mnss.core.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.novaalianca.mnss.core.shared.Money;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductTest {
    @Test
    void inactiveProductCannotBeSoldOnAnyChannel() {
        Product product = new Product(
                UUID.randomUUID(),
                "Pao frances",
                Money.of("1.50"),
                UnitType.UNIT,
                PreparationSector.BALCAO,
                false,
                true,
                true,
                true,
                true);

        assertThat(product.canSellOn(SalesChannel.PDV)).isFalse();
        assertThat(product.canSellOn(SalesChannel.SITE)).isFalse();
        assertThat(product.canSellOn(SalesChannel.WHATSAPP)).isFalse();
    }

    @Test
    void unavailableProductCanRemainOnPdvButCannotBeSoldOnline() {
        Product product = new Product(
                UUID.randomUUID(),
                "Bolo de cenoura",
                Money.of("12.00"),
                UnitType.SLICE,
                PreparationSector.CONFEITARIA,
                true,
                false,
                true,
                true,
                true);

        assertThat(product.canSellOn(SalesChannel.PDV)).isTrue();
        assertThat(product.canSellOn(SalesChannel.SITE)).isFalse();
        assertThat(product.canSellOn(SalesChannel.WHATSAPP)).isFalse();
    }
}
