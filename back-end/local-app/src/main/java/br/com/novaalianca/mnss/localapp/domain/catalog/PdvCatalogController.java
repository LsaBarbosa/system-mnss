package br.com.novaalianca.mnss.localapp.domain.catalog;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdv/products")
class PdvCatalogController {
    private final CatalogService catalogService;

    PdvCatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    List<CategoryProductsResponse> listPdvProducts() {
        return catalogService.listPdvProducts();
    }
}
