package br.com.novaalianca.mnss.localapp.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
class SecurityWebConfiguration implements WebMvcConfigurer {
}
