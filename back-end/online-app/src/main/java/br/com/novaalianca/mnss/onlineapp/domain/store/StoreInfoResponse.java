package br.com.novaalianca.mnss.onlineapp.domain.store;

public record StoreInfoResponse(
        String name,
        String address,
        String hours,
        String phone,
        String description) {}
