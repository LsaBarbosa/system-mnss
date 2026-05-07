package br.com.novaalianca.mnss.onlineapp.security.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

        OnlineUserDetails userDetails = (OnlineUserDetails) auth.getPrincipal();

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roles.isEmpty()) {
            roles = Set.of("USER");
        }

        String token = tokenProvider.generateToken(userDetails.getUsername(), roles);
        Instant expiresAt = Instant.now().plus(8, ChronoUnit.HOURS);

        return new AuthResponse(
                token,
                expiresAt.toString(),
                new AuthUserResponse(
                        userDetails.getId().toString(),
                        userDetails.getName(),
                        userDetails.getEmail() != null ? userDetails.getEmail()
                                : userDetails.getUsername() + "@novaalianca.local",
                        userDetails.getUsername(),
                        userDetails.isEnabled(),
                        roles
                )
        );
    }
}
