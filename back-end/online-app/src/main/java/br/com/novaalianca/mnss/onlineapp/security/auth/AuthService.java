package br.com.novaalianca.mnss.onlineapp.security.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = tokenProvider.generateToken(request.username());
        Instant expiresAt = Instant.now().plus(8, ChronoUnit.HOURS);

        return new AuthResponse(
                token,
                expiresAt.toString(),
                new AuthUserResponse(
                        UUID.randomUUID().toString(),
                        request.username(),
                        request.username() + "@novaalianca.local",
                        request.username(),
                        true,
                        true
                )
        );
    }
}
