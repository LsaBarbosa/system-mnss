package br.com.novaalianca.mnss.sharedinfra.health;

import java.time.Clock;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = HealthEndpointAutoConfiguration.class)
@ConditionalOnClass(HealthEndpoint.class)
public class HealthMonitoringAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    Clock mnssClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnBean(HealthEndpoint.class)
    @ConditionalOnMissingBean
    TechnicalHealthService technicalHealthService(
            HealthEndpoint healthEndpoint,
            Clock clock,
            @Value("${app.version:0.0.1-MVP}") String version) {
        return new TechnicalHealthService(healthEndpoint, clock, version);
    }
}
