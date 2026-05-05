package br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank(message = "O conteúdo da mensagem é obrigatório")
        String content) {}
