package br.com.novaalianca.mnss.localapp.security.config;

import br.com.novaalianca.mnss.localapp.security.auth.AuthService;
import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUserInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
class SecurityWebConfiguration implements WebMvcConfigurer {
    private final AuthService authService;

    SecurityWebConfiguration(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticatedUserInterceptor(authService))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/health",
                        "/api/ping",
                        "/api/public/**");
    }
}
