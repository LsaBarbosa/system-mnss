package br.com.novaalianca.mnss.onlineapp.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "A rua é obrigatória")
        @Size(max = 150)
        String street,

        @Size(max = 30)
        String number,

        @Size(max = 120)
        String neighborhood,

        @Size(max = 120)
        String city,

        @Size(max = 60)
        String state,

        @Size(max = 20)
        String zipCode,

        @Size(max = 120)
        String complement,

        String reference
) {}
