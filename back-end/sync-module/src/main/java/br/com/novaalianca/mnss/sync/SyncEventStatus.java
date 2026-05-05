package br.com.novaalianca.mnss.sync;

public enum SyncEventStatus {
    PENDING,
    PROCESSING,
    SYNCED,
    FAILED,
    RETRYING,
    DEAD_LETTER,
    IGNORED
}
