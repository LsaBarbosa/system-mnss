package br.com.novaalianca.mnss.onlineapp.config;

import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Validates HMAC secret strength at startup. Enforced only in the 'online'
 * (production) profile. In dev/test, weak secrets are tolerated.
 */
@Component
@Profile("online")
class HmacSecretStrengthValidator {
    private static final Logger log = LoggerFactory.getLogger(HmacSecretStrengthValidator.class);

    private final SyncStoresProperties storesProperties;
    private final String webhookSecret;
    private final String syncMasterSecret;

    HmacSecretStrengthValidator(
            SyncStoresProperties storesProperties,
            @Value("${mnss.payment.webhook-secret}") String webhookSecret,
            @Value("${mnss.online.sync-master-secret:}") String syncMasterSecret) {
        this.storesProperties = storesProperties;
        this.webhookSecret = webhookSecret;
        this.syncMasterSecret = syncMasterSecret;
    }

    @PostConstruct
    void validate() {
        log.info("Validating HMAC secret strength for production profile...");
        HmacUtils.validateSecretStrength(webhookSecret, "mnss.payment.webhook-secret");
        if (!syncMasterSecret.isBlank()) {
            HmacUtils.validateSecretStrength(syncMasterSecret, "mnss.online.sync-master-secret");
        }
        storesProperties.stores().forEach((storeId, secret) ->
                HmacUtils.validateSecretStrength(secret, "mnss.sync.stores." + storeId));
        log.info("HMAC secret strength validation passed.");
    }
}
