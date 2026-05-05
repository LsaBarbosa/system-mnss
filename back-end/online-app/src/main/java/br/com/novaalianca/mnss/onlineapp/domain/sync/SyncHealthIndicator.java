package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

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
            
            Health.Builder status = (failedCount > 50) ? Health.down() : Health.up();

            return status
                    .withDetail("pending", pendingCount)
                    .withDetail("failed", failedCount)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
