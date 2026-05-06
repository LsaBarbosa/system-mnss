package br.com.novaalianca.mnss.sharedinfra.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OpenApiValidationTest {

    private final OpenAPIV3Parser parser = new OpenAPIV3Parser();

    @ParameterizedTest
    @ValueSource(strings = {
            "docs/contracts/local-api.openapi.yml",
            "docs/contracts/online-api.openapi.yml"
    })
    @DisplayName("OpenAPI syntax should be valid")
    void openApiSyntaxShouldBeValid(String contractPath) {
        Path path = getContractPath(contractPath);
        SwaggerParseResult result = parser.readLocation(path.toString(), null, null);

        assertThat(result.getMessages())
                .withFailMessage("OpenAPI validation errors in %s: %s", contractPath, result.getMessages())
                .isEmpty();

        assertThat(result.getOpenAPI())
                .withFailMessage("Failed to parse OpenAPI for %s", contractPath)
                .isNotNull();
    }

    @Test
    @DisplayName("Local API should contain essential routes")
    void localApiShouldContainEssentialRoutes() {
        OpenAPI openAPI = parseContract("docs/contracts/local-api.openapi.yml");
        List<String> paths = openAPI.getPaths().keySet().stream().toList();

        assertThat(paths).contains(
                "/api/auth/login",
                "/api/health",
                "/api/pdv/sales",
                "/api/kds/tickets"
        );
    }

    @Test
    @DisplayName("Online API should contain essential routes")
    void onlineApiShouldContainEssentialRoutes() {
        OpenAPI openAPI = parseContract("docs/contracts/online-api.openapi.yml");
        List<String> paths = openAPI.getPaths().keySet().stream().toList();

        assertThat(paths).contains(
                "/api/health",
                "/api/sync/events"
        );
    }

    private OpenAPI parseContract(String contractPath) {
        Path path = getContractPath(contractPath);
        SwaggerParseResult result = parser.readLocation(path.toString(), null, null);
        return result.getOpenAPI();
    }

    private Path getContractPath(String relativePath) {
        // Resolve from project root
        Path root = Paths.get("").toAbsolutePath();
        while (root != null && !new File(root.toFile(), "AGENTS.md").exists()) {
            root = root.getParent();
        }
        
        if (root == null) {
             // Fallback for different build environments
             root = Paths.get(System.getProperty("user.dir"));
        }
        
        return root.resolve(relativePath);
    }
}
