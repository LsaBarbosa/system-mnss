package br.com.novaalianca.mnss.localapp.domain.shared;

import java.math.BigDecimal;
import java.util.Objects;

public final class DomainValidation {
    private DomainValidation() {}

    public static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
        return value;
    }

    public static BigDecimal requirePositive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    public static BigDecimal optionalNonNegative(BigDecimal value, String field) {
        if (value == null) {
            return null;
        }
        return requireNonNegative(value, field);
    }
}
