package br.com.novaalianca.mnss.localapp.domain.sync;

public enum SyncStatus {
    PENDING,
    PROCESSING,
    SYNCED,
    FAILED,
    RETRYING,
    IGNORED
}
