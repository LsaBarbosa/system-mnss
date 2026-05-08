package br.com.novaalianca.mnss.onlineapp.security.config;

import br.com.novaalianca.mnss.onlineapp.security.auth.JwtAuthenticationFilter;
import br.com.novaalianca.mnss.onlineapp.security.auth.JwtTokenProvider;
import br.com.novaalianca.mnss.onlineapp.security.auth.OnlineUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    private final JwtTokenProvider jwtTokenProvider;
    private final CorsConfigurationSource corsConfigurationSource;
    private final boolean hstsEnabled;

    public SecurityConfiguration(
            JwtTokenProvider jwtTokenProvider,
            CorsConfigurationSource corsConfigurationSource,
            @Value("${mnss.security.hsts-enabled:true}") boolean hstsEnabled) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.corsConfigurationSource = corsConfigurationSource;
        this.hstsEnabled = hstsEnabled;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentTypeOptions(config -> {})
                        .referrerPolicy(referrerPolicy -> referrerPolicy.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/health",
                                "/api/version",
                                "/api/public/**",
                                "/api/auth/login",
                                "/api/whatsapp/webhook",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sync/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/sync/pending").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sync/events/*/ack").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable);

        if (hstsEnabled) {
            http.headers(headers -> headers
                    .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000).includeSubDomains(true)));
        } else {
            http.headers(headers -> headers.httpStrictTransportSecurity(hsts -> hsts.disable()));
        }

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
            OnlineUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return builder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
