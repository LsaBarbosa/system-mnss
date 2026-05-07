package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
class CategoryController {
    private final CatalogService catalogService;

    CategoryController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    List<CategoryResponse> listCategories(
            @RequestParam(name = "channel", defaultValue = "ADMIN") CatalogChannel channel) {
        return catalogService.listCategories(channel);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    CategoryResponse createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            HttpServletRequest servletRequest) {
        return catalogService.createCategory(request, authenticatedUserId(servletRequest));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    CategoryResponse updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody PatchCategoryRequest request,
            HttpServletRequest servletRequest) {
        return catalogService.updateCategory(id, request, authenticatedUserId(servletRequest));
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUser.AUTHENTICATED_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser user ? user.id() : null;
    }
}
