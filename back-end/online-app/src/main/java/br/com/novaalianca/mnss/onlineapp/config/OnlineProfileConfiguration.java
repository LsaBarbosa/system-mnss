package br.com.novaalianca.mnss.onlineapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("online")
@EnableConfigurationProperties({OnlineProfileProperties.class, SyncStoresProperties.class})
public class OnlineProfileConfiguration {
}
