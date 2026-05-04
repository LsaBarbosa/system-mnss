package br.com.novaalianca.mnss.sharedinfra.backup;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class BackupMetadataService {
    private final Clock clock;

    public BackupMetadataService(Clock clock) {
        this.clock = clock;
    }

    public BackupMetadata calculate(Optional<Instant> lastBackupAt, Duration maxAge) {
        if (maxAge.isZero() || maxAge.isNegative()) {
            throw new IllegalArgumentException("maxAge must be positive");
        }

        Instant checkedAt = Instant.now(clock);
        BackupFreshnessStatus status = lastBackupAt
                .map(lastBackup -> lastBackup.isBefore(checkedAt.minus(maxAge))
                        ? BackupFreshnessStatus.LATE
                        : BackupFreshnessStatus.VALID)
                .orElse(BackupFreshnessStatus.MISSING);

        return new BackupMetadata(lastBackupAt.orElse(null), checkedAt, maxAge, status);
    }
}
