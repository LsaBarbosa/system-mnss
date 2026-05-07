package br.com.novaalianca.mnss.onlineapp.security.auth;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthUserResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        Set<String> roles = auth == null
                ? Set.of()
                : auth.getAuthorities().stream()
                .map(granted -> granted.getAuthority())
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roles.isEmpty() && auth != null && auth.isAuthenticated()) {
            roles = Set.of("ADMIN");
        }
        return new AuthUserResponse(
                UUID.randomUUID().toString(),
                username,
                username + "@novaalianca.local",
                username,
                true,
                roles
        );
    }
}
