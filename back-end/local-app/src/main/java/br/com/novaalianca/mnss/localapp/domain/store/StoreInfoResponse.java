package br.com.novaalianca.mnss.localapp.domain.store;

public record StoreInfoResponse(
        String name,
        String address,
        String hours,
        String phone,
        String description) {}
