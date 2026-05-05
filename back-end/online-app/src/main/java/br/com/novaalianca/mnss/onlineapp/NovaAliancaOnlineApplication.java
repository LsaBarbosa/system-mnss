package br.com.novaalianca.mnss.onlineapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NovaAliancaOnlineApplication {
    public static void main(String[] args) {
        SpringApplication.run(NovaAliancaOnlineApplication.class, args);
    }
}
