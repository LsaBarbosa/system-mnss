package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUserInterceptor;
import br.com.novaalianca.mnss.localapp.security.auth.RequiresRole;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
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
@RequestMapping("/api/products")
class ProductController {
    private final CatalogService catalogService;

    ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    List<ProductResponse> listProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID categoryId) {
        return catalogService.listProducts(name, categoryId);
    }

    @GetMapping("/barcode/{barcode}")
    ProductResponse findProductByBarcode(@PathVariable String barcode) {
        return catalogService.findSellableProductByBarcode(barcode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresRole({RoleName.GERENTE})
    ProductResponse createProduct(
            @Valid @RequestBody CreateProductRequest request,
            HttpServletRequest servletRequest) {
        return catalogService.createProduct(request, authenticatedUserId(servletRequest));
    }

    @PatchMapping("/{id}")
    @RequiresRole({RoleName.GERENTE})
    ProductResponse updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody PatchProductRequest request,
            HttpServletRequest servletRequest) {
        return catalogService.updateProduct(id, request, authenticatedUserId(servletRequest));
    }

    @PatchMapping("/{id}/availability")
    @RequiresRole({RoleName.GERENTE, RoleName.ATENDENTE})
    ProductAvailabilityResponse updateProductAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody PatchProductAvailabilityRequest request,
            HttpServletRequest servletRequest) {
        return catalogService.updateProductAvailability(id, request, authenticatedUserId(servletRequest));
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthenticatedUserInterceptor.AUTHENTICATED_USER_ATTRIBUTE);
        return attribute instanceof AuthenticatedUser user ? user.id() : null;
    }
}
