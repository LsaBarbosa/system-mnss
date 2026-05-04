package br.com.novaalianca.mnss.sharedinfra.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BackupMetadataServiceTest {
    private static final Instant NOW = Instant.parse("2026-05-04T12:00:00Z");
    private final BackupMetadataService service =
            new BackupMetadataService(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void marksRecentBackupAsValid() {
        BackupMetadata metadata = service.calculate(Optional.of(NOW.minus(Duration.ofHours(8))), Duration.ofDays(1));

        assertThat(metadata.status()).isEqualTo(BackupFreshnessStatus.VALID);
        assertThat(metadata.checkedAt()).isEqualTo(NOW);
    }

    @Test
    void marksOldBackupAsLate() {
        BackupMetadata metadata = service.calculate(Optional.of(NOW.minus(Duration.ofDays(2))), Duration.ofDays(1));

        assertThat(metadata.status()).isEqualTo(BackupFreshnessStatus.LATE);
    }

    @Test
    void marksMissingBackupExplicitly() {
        BackupMetadata metadata = service.calculate(Optional.empty(), Duration.ofDays(1));

        assertThat(metadata.status()).isEqualTo(BackupFreshnessStatus.MISSING);
        assertThat(metadata.lastBackupAt()).isNull();
    }

    @Test
    void rejectsNonPositiveMaxAge() {
        assertThatThrownBy(() -> service.calculate(Optional.empty(), Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxAge must be positive");
    }
}
