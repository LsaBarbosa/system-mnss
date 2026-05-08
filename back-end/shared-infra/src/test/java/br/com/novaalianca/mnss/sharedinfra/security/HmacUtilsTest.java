package br.com.novaalianca.mnss.sharedinfra.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HmacUtilsTest {

    private static final String SECRET = "test-secret-key-for-hmac-sha256-validation";
    private static final String DATA = "idempotency-key:payload-json";

    @Test
    void calculateHmacReturnsStableValue() {
        String first = HmacUtils.calculateHmac(DATA, SECRET);
        String second = HmacUtils.calculateHmac(DATA, SECRET);
        assertThat(first).isEqualTo(second);
        assertThat(first).isNotBlank();
    }

    @Test
    void calculateHmacProducesDifferentValueForDifferentData() {
        String hmac1 = HmacUtils.calculateHmac("data-a", SECRET);
        String hmac2 = HmacUtils.calculateHmac("data-b", SECRET);
        assertThat(hmac1).isNotEqualTo(hmac2);
    }

    @Test
    void verifyHmacReturnsTrueForValidSignature() {
        String hmac = HmacUtils.calculateHmac(DATA, SECRET);
        assertThat(HmacUtils.verifyHmac(DATA, hmac, SECRET)).isTrue();
    }

    @Test
    void verifyHmacReturnsFalseForInvalidSignature() {
        assertThat(HmacUtils.verifyHmac(DATA, "invalid-signature", SECRET)).isFalse();
    }

    @Test
    void verifyHmacReturnsFalseForTruncatedSignature() {
        String hmac = HmacUtils.calculateHmac(DATA, SECRET);
        String truncated = hmac.substring(0, hmac.length() / 2);
        assertThat(HmacUtils.verifyHmac(DATA, truncated, SECRET)).isFalse();
    }

    @Test
    void verifyHmacReturnsFalseForNullSignature() {
        assertThat(HmacUtils.verifyHmac(DATA, null, SECRET)).isFalse();
    }

    @Test
    void verifyHmacReturnsFalseForNullData() {
        String hmac = HmacUtils.calculateHmac(DATA, SECRET);
        assertThat(HmacUtils.verifyHmac(null, hmac, SECRET)).isFalse();
    }

    @Test
    void verifyHmacReturnsFalseForNullSecret() {
        String hmac = HmacUtils.calculateHmac(DATA, SECRET);
        assertThat(HmacUtils.verifyHmac(DATA, hmac, null)).isFalse();
    }

    @Test
    void verifyHmacReturnsFalseForWrongSecret() {
        String hmac = HmacUtils.calculateHmac(DATA, SECRET);
        assertThat(HmacUtils.verifyHmac(DATA, hmac, "wrong-secret")).isFalse();
    }

    @Test
    void calculateHmacThrowsForNullData() {
        assertThatThrownBy(() -> HmacUtils.calculateHmac(null, SECRET))
                .isInstanceOf(RuntimeException.class);
    }
}
