package br.com.novaalianca.mnss.onlineapp.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class HmacSecretStrengthValidatorTest {

    private static final String STRONG_SECRET = "a-very-long-secret-with-more-than-32-bytes-ok!";
    private static final String WEAK_SECRET = "tooshort";

    private HmacSecretStrengthValidator validator(String webhookSecret, String syncMasterSecret,
            Map<String, String> stores) {
        return new HmacSecretStrengthValidator(
                new SyncStoresProperties(stores), webhookSecret, syncMasterSecret);
    }

    @Test
    void validatesSuccessfullyWhenAllSecretsAreStrong() {
        HmacSecretStrengthValidator v = validator(
                STRONG_SECRET, STRONG_SECRET, Map.of("store-001", STRONG_SECRET));
        assertThatCode(v::validate).doesNotThrowAnyException();
    }

    @Test
    void failsWhenWebhookSecretIsTooShort() {
        HmacSecretStrengthValidator v = validator(
                WEAK_SECRET, STRONG_SECRET, Map.of("store-001", STRONG_SECRET));
        assertThatThrownBy(v::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mnss.payment.webhook-secret");
    }

    @Test
    void failsWhenStoreSecretIsTooShort() {
        HmacSecretStrengthValidator v = validator(
                STRONG_SECRET, STRONG_SECRET, Map.of("store-001", WEAK_SECRET));
        assertThatThrownBy(v::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mnss.sync.stores.store-001");
    }

    @Test
    void failsWhenSyncMasterSecretIsTooShort() {
        HmacSecretStrengthValidator v = validator(
                STRONG_SECRET, WEAK_SECRET, Map.of("store-001", STRONG_SECRET));
        assertThatThrownBy(v::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mnss.online.sync-master-secret");
    }

    @Test
    void skipsSyncMasterSecretValidationWhenBlank() {
        HmacSecretStrengthValidator v = validator(
                STRONG_SECRET, "", Map.of("store-001", STRONG_SECRET));
        assertThatCode(v::validate).doesNotThrowAnyException();
    }
}
