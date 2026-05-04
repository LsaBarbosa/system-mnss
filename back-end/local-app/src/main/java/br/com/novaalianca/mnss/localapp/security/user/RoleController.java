package br.com.novaalianca.mnss.localapp.security.user;

import br.com.novaalianca.mnss.localapp.security.auth.RequiresRole;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@RequiresRole(RoleName.ADMIN)
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
class RoleController {
    private final UserManagementService userManagementService;

    RoleController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    List<RoleResponse> listRoles() {
        return userManagementService.listRoles();
    }
}
