package br.com.novaalianca.mnss.sharedinfra.web.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        String code,
        String message,
        int status,
        String path,
        Instant timestamp,
        List<ApiFieldError> validationErrors) {

    public static ApiError withoutFields(String code, String message, int status, String path) {
        return new ApiError(code, message, status, path, Instant.now(), List.of());
    }

    public static ApiError withFields(
            String code,
            String message,
            int status,
            String path,
            List<ApiFieldError> validationErrors) {
        return new ApiError(code, message, status, path, Instant.now(), List.copyOf(validationErrors));
    }
}
