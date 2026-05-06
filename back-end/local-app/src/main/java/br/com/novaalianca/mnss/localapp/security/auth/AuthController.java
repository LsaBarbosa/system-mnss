package br.com.novaalianca.mnss.localapp.security.auth;

import br.com.novaalianca.mnss.localapp.security.user.UserResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
class AuthController {
    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    UserResponse me(@RequestAttribute(AuthenticatedUser.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
        return authService.toResponse(user);
    }
}
