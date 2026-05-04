package br.com.novaalianca.mnss.localapp.ping;

import java.time.Instant;

record PingResponse(String message, String application, String environment, Instant checkedAt) {}
