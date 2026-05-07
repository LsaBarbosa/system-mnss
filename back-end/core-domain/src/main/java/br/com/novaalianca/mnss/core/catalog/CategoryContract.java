package br.com.novaalianca.mnss.core.catalog;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato compartilhado para Category entre local-app e online-app.
 */
public interface CategoryContract {
    UUID getId();
    String getName();
    String getDescription();
    boolean isActive();
    boolean isShowOnPdv();
    boolean isShowOnline();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
