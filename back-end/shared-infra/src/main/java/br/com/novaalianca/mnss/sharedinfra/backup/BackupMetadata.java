package br.com.novaalianca.mnss.sharedinfra.backup;

import java.time.Duration;
import java.time.Instant;

public record BackupMetadata(
        Instant lastBackupAt,
        Instant checkedAt,
        Duration maxAge,
        BackupFreshnessStatus status) {
}
