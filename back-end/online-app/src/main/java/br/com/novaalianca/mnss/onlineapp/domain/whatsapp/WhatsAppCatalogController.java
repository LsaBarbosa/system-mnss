package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductRepository;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.PublicProductResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Authenticated endpoint for attendants to browse products enabled for the WhatsApp channel.
 */
@RestController
@RequestMapping("/api/whatsapp/catalog")
public class WhatsAppCatalogController {

    private final OnlineProductRepository productRepository;

    public WhatsAppCatalogController(OnlineProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<PublicProductResponse> listWhatsAppProducts() {
        return productRepository.findByActiveTrueAndAvailableTrueAndSellOnWhatsappTrueOrderByNameAsc()
                .stream()
                .map(PublicProductResponse::from)
                .toList();
    }
}
