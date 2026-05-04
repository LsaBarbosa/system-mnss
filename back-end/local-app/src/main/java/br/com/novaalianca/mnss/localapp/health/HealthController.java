package br.com.novaalianca.mnss.localapp.health;

import br.com.novaalianca.mnss.sharedinfra.health.TechnicalHealthResponse;
import br.com.novaalianca.mnss.sharedinfra.health.TechnicalHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
class HealthController {
    private final TechnicalHealthService technicalHealthService;

    HealthController(TechnicalHealthService technicalHealthService) {
        this.technicalHealthService = technicalHealthService;
    }

    @GetMapping
    TechnicalHealthResponse health() {
        return technicalHealthService.inspect(
                "local",
                true,
                "PDV, caixa, KDS e banco local devem operar sem internet");
    }
}
