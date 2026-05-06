package br.com.novaalianca.mnss.localapp.security.config;

import br.com.novaalianca.mnss.localapp.security.auth.AuthService;
import br.com.novaalianca.mnss.localapp.security.auth.JwtAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfiguration {

    private final AuthService authService;

    public SecurityConfiguration(AuthService authService) {
        this.authService = authService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/health", "/api/ping", "/api/public/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(new JwtAuthenticationFilter(authService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
