package br.com.novaalianca.mnss.localapp.security.auth;

import br.com.novaalianca.mnss.localapp.security.user.UserEntity;
import br.com.novaalianca.mnss.localapp.security.user.UserMapper;
import br.com.novaalianca.mnss.localapp.security.user.UserRepository;
import br.com.novaalianca.mnss.localapp.security.user.UserResponse;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthTokenService authTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AuthTokenService authTokenService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.authTokenService = authTokenService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> invalidCredentials(request.username()));

        if (!passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials(request.username());
        }

        if (!user.isActive()) {
            LOGGER.warn("Inactive user login blocked for username={}", request.username());
            throw new BusinessException("USER_INACTIVE", "Usuario inativo.", HttpStatus.UNAUTHORIZED);
        }

        if (user.getRoles().isEmpty()) {
            throw new BusinessException("USER_WITHOUT_ROLE", "Usuario sem perfil operacional.", HttpStatus.UNAUTHORIZED);
        }

        AuthTokenClaims claims = authTokenService.issue(user.getId(), user.getUsername());
        return new AuthResponse(
                authTokenService.serialize(claims),
                claims.expiresAt(),
                UserMapper.toResponse(user));
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser authenticate(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("AUTH_REQUIRED", "Autenticacao obrigatoria.", HttpStatus.UNAUTHORIZED);
        }

        AuthTokenClaims claims = authTokenService.validate(authorizationHeader.substring(BEARER_PREFIX.length()));
        UserEntity user = userRepository.findByUsername(claims.username())
                .orElseThrow(() -> new BusinessException("AUTH_TOKEN_INVALID", "Token invalido.", HttpStatus.UNAUTHORIZED));
        if (!user.isActive()) {
            throw new BusinessException("USER_INACTIVE", "Usuario inativo.", HttpStatus.UNAUTHORIZED);
        }
        return toAuthenticatedUser(user);
    }

    public UserResponse toResponse(AuthenticatedUser user) {
        return new UserResponse(user.id(), user.name(), user.email(), user.username(), user.active(), user.roles());
    }

    private AuthenticatedUser toAuthenticatedUser(UserEntity user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.isActive(),
                UserMapper.roleNames(user));
    }

    private BusinessException invalidCredentials(String username) {
        LOGGER.warn("Invalid login attempt for username={}", username);
        return new BusinessException("INVALID_CREDENTIALS", "Credenciais invalidas.", HttpStatus.UNAUTHORIZED);
    }
}
