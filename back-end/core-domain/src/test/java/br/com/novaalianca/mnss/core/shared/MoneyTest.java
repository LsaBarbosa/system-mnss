package br.com.novaalianca.mnss.core.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {
    @Test
    void normalizesToDatabaseScale() {
        Money money = Money.of("10.555");

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10.56"));
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThatThrownBy(() -> Money.of("-0.01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must not be negative");
    }
}
