package br.com.novaalianca.mnss.core.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    public static final int SCALE = 2;
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = normalize(amount);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(String amount) {
        return of(new BigDecimal(amount));
    }

    public BigDecimal amount() {
        return amount;
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        return new Money(amount.add(other.amount));
    }

    public Money subtract(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        return new Money(amount.subtract(other.amount));
    }

    public boolean isZero() {
        return BigDecimal.ZERO.compareTo(amount) == 0;
    }

    @Override
    public int compareTo(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        return amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }
        if (!(candidate instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }

    private static BigDecimal normalize(BigDecimal value) {
        Objects.requireNonNull(value, "amount must not be null");
        BigDecimal normalized = value.setScale(SCALE, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        return normalized;
    }
}
