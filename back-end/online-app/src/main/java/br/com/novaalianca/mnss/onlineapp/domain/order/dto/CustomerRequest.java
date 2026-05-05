package br.com.novaalianca.mnss.onlineapp.domain.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150)
        String name,

        @NotBlank(message = "O telefone é obrigatório")
        @Size(max = 30)
        String phone,

        @Email(message = "Email inválido")
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String document
) {}
