package br.com.novaalianca.mnss.onlineapp.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/version")
public class VersionController {

    @Value("${app.version:0.0.1-MVP}")
    private String version;

    @GetMapping
    public Map<String, String> getVersion() {
        return Map.of("version", version);
    }
}
