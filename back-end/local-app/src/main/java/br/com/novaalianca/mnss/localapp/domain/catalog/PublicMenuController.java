package br.com.novaalianca.mnss.localapp.domain.catalog;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/menu")
class PublicMenuController {
    private final CatalogService catalogService;

    PublicMenuController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    List<CategoryProductsResponse> listPublicMenu(@org.springframework.web.bind.annotation.RequestParam(required = false) String search) {
        return catalogService.listPublicMenu(search);
    }

    @GetMapping("/info")
    br.com.novaalianca.mnss.localapp.domain.store.StoreInfoResponse getStoreInfo() {
        return catalogService.getStoreInfo();
    }
}
