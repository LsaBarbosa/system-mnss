package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import br.com.novaalianca.mnss.onlineapp.domain.store.StoreInfoResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicMenuController {
    private final PublicMenuService publicMenuService;

    public PublicMenuController(PublicMenuService publicMenuService) {
        this.publicMenuService = publicMenuService;
    }

    @GetMapping("/menu")
    public List<PublicMenuResponse> listPublicMenu(@RequestParam(required = false) String search) {
        return publicMenuService.listMenu(search);
    }

    @GetMapping("/info")
    public StoreInfoResponse getStoreInfo() {
        return publicMenuService.getStoreInfo();
    }
}
