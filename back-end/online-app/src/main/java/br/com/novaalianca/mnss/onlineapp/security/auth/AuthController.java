package br.com.novaalianca.mnss.onlineapp.security.auth;

import br.com.novaalianca.mnss.onlineapp.domain.user.OnlineUserRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final OnlineUserRepository userRepository;

    public AuthController(AuthService authService, OnlineUserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthUserResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        return userRepository.findByUsername(username)
                .map(user -> {
                    Set<String> roles = user.getRoles().stream()
                            .map(r -> r.getName())
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    return new AuthUserResponse(
                            user.getId().toString(),
                            user.getName(),
                            user.getEmail() != null ? user.getEmail()
                                    : user.getUsername() + "@novaalianca.local",
                            user.getUsername(),
                            user.isActive(),
                            roles
                    );
                })
                .orElseGet(() -> {
                    Set<String> roles = auth == null ? Set.of()
                            : auth.getAuthorities().stream()
                                    .map(a -> a.getAuthority().startsWith("ROLE_")
                                            ? a.getAuthority().substring(5) : a.getAuthority())
                                    .collect(Collectors.toCollection(LinkedHashSet::new));
                    return new AuthUserResponse(null, username, null, username, true, roles);
                });
    }
}
