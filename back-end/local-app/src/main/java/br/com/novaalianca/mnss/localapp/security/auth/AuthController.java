package br.com.novaalianca.mnss.localapp.security.auth;

import br.com.novaalianca.mnss.localapp.security.user.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

    static final String COOKIE_NAME = "mnss_auth";

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse servletResponse) {
        AuthResponse authResponse = authService.login(request);
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, authCookie(authResponse.token(), Duration.ofHours(8)).toString());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletResponse servletResponse) {
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, authCookie("", Duration.ZERO).toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    UserResponse me(@RequestAttribute(AuthenticatedUser.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
        return authService.toResponse(user);
    }

    private ResponseCookie authCookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
