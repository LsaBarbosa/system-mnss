package br.com.novaalianca.mnss.localapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("local")
@EnableConfigurationProperties(LocalProfileProperties.class)
class LocalProfileConfiguration {
}
