package br.com.novaalianca.mnss.onlineapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication(scanBasePackages = {
    "br.com.novaalianca.mnss.onlineapp",
    "br.com.novaalianca.mnss.sync"
})
@EntityScan(basePackages = {
    "br.com.novaalianca.mnss.onlineapp",
    "br.com.novaalianca.mnss.sync"
})
public class NovaAliancaOnlineApplication {
    public static void main(String[] args) {
        SpringApplication.run(NovaAliancaOnlineApplication.class, args);
    }
}
