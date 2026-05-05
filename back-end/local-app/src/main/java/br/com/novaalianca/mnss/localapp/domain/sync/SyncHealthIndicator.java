package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SyncHealthIndicator implements HealthIndicator {

    private final SyncEventRepository repository;

    public SyncHealthIndicator(SyncEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public Health health() {
        try {
            long pendingCount = repository.countByStatus(SyncEventStatus.PENDING);
            long failedCount = repository.countByStatus(SyncEventStatus.FAILED);
            long retryingCount = repository.countByStatus(SyncEventStatus.RETRYING);
            long deadLetterCount = repository.countByStatus(SyncEventStatus.DEAD_LETTER);

            Health.Builder status = (failedCount > 10 || deadLetterCount > 0) ? Health.down() : Health.up();

            return status
                    .withDetail("pending", pendingCount)
                    .withDetail("failed", failedCount)
                    .withDetail("retrying", retryingCount)
                    .withDetail("deadLetter", deadLetterCount)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
