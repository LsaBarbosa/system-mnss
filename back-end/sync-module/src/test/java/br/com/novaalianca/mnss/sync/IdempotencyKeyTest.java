package br.com.novaalianca.mnss.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class IdempotencyKeyTest {
    @Test
    void normalizesKeyForStableDeduplication() {
        IdempotencyKey key = new IdempotencyKey("  ORDER:ABC-123  ");

        assertThat(key.value()).isEqualTo("order:abc-123");
    }

    @Test
    void rejectsBlankKey() {
        assertThatThrownBy(() -> new IdempotencyKey(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("idempotency key must not be blank");
    }
}
