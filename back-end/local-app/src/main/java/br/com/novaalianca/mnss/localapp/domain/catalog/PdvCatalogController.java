package br.com.novaalianca.mnss.localapp.domain.catalog;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdv/products")
class PdvCatalogController {
    private final CatalogService catalogService;

    PdvCatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    List<CategoryProductsResponse> listPdvProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID categoryId) {
        return catalogService.listPdvProducts(name, categoryId);
    }

    @GetMapping("/barcode/{barcode}")
    ProductResponse findPdvProductByBarcode(@PathVariable String barcode) {
        return catalogService.findSellableProductByBarcode(barcode);
    }
}
