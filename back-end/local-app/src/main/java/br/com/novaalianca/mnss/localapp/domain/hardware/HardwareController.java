package br.com.novaalianca.mnss.localapp.domain.hardware;

import org.springframework.security.access.prepost.PreAuthorize;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hardware")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'CAIXA', 'ATENDENTE')")
class HardwareController {
    private final HardwareAdapterService hardwareAdapterService;

    HardwareController(HardwareAdapterService hardwareAdapterService) {
        this.hardwareAdapterService = hardwareAdapterService;
    }

    @PostMapping("/drawer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void openDrawer() {
        hardwareAdapterService.openDrawer();
    }
}
