package br.com.novaalianca.mnss.sharedinfra.web.error;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class ErrorHandlingAutoConfiguration {
}
