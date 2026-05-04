package br.com.novaalianca.mnss.localapp.security.user;

import br.com.novaalianca.mnss.localapp.security.auth.RequiresRole;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiresRole(RoleName.ADMIN)
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
class UserController {
    private final UserManagementService userManagementService;

    UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    List<UserResponse> listUsers() {
        return userManagementService.listUsers();
    }

    @PostMapping
    UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userManagementService.createUser(request);
    }
}
