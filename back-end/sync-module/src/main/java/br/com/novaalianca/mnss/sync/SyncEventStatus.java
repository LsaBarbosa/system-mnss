package br.com.novaalianca.mnss.sync;

public enum SyncEventStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED,
    DEAD_LETTER
}
