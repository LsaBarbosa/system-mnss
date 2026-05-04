package br.com.novaalianca.mnss.localapp.security.auth;

import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/critical-actions")
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
class CriticalActionController {
    @PostMapping("/authorize")
    @RequiresRole(RoleName.GERENTE)
    ResponseEntity<Void> authorizeCriticalAction() {
        return ResponseEntity.noContent().build();
    }
}
