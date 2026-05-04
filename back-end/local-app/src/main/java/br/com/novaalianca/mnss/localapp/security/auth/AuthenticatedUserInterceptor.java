package br.com.novaalianca.mnss.localapp.security.auth;

import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthenticatedUserInterceptor implements HandlerInterceptor {
    public static final String AUTHENTICATED_USER_ATTRIBUTE = "mnssAuthenticatedUser";
    private final AuthService authService;

    public AuthenticatedUserInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuthenticatedUser user = authService.authenticate(request.getHeader(HttpHeaders.AUTHORIZATION));
        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, user);
        enforceRole(user, handler);
        return true;
    }

    private void enforceRole(AuthenticatedUser user, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }

        RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
        if (requiresRole == null) {
            requiresRole = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
        }
        if (requiresRole == null) {
            return;
        }

        Set<RoleName> requiredRoles = Arrays.stream(requiresRole.value()).collect(Collectors.toSet());
        if (!user.hasAnyRole(requiredRoles)) {
            throw new BusinessException("FORBIDDEN", "Permissao insuficiente.", HttpStatus.FORBIDDEN);
        }
    }
}
