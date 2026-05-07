package br.com.novaalianca.mnss.onlineapp.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "mnss.sync")
public record SyncStoresProperties(@NotEmpty Map<String, String> stores) {

    public String secretFor(String storeId) {
        return stores.get(storeId);
    }
}
