package br.com.novaalianca.mnss.sharedinfra.backup;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class BackupMetadataAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    BackupMetadataService backupMetadataService(Clock clock) {
        return new BackupMetadataService(clock);
    }
}
