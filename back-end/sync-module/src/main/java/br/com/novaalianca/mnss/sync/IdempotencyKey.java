package br.com.novaalianca.mnss.sync;

import java.util.Locale;
import java.util.Objects;

public record IdempotencyKey(String value) {
    public IdempotencyKey {
        Objects.requireNonNull(value, "value must not be null");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.isBlank()) {
            throw new IllegalArgumentException("idempotency key must not be blank");
        }
        if (value.length() > 120) {
            throw new IllegalArgumentException("idempotency key must have at most 120 characters");
        }
    }
}
