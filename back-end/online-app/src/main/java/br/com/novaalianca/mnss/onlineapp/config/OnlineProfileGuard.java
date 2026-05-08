package br.com.novaalianca.mnss.onlineapp.config;

import java.util.Arrays;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class OnlineProfileGuard implements ApplicationRunner {
    private final Environment environment;

    public OnlineProfileGuard(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean onlineEnvironment = "online".equalsIgnoreCase(environment.getProperty("mnss.environment"));
        boolean onlineProfileActive = Arrays.asList(environment.getActiveProfiles()).contains("online");

        if (onlineEnvironment && !onlineProfileActive) {
            throw new IllegalStateException("SPRING_PROFILES_ACTIVE=online is required for online-app");
        }
    }
}
