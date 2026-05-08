package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Garante que WHATSAPP_PROVIDER=mock é o comportamento padrão e que nenhum
 * provider real é ativado sem configuração explícita.
 *
 * Provider real de WhatsApp está fora do escopo atual — deve ser implementado
 * e homologado antes de ser habilitado em produção.
 */
class WhatsAppProviderDefaultConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(MockWhatsAppProvider.class);

    @Test
    void mockProviderIsActiveByDefault() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(WhatsAppProviderPort.class);
            assertThat(ctx.getBean(WhatsAppProviderPort.class))
                    .isInstanceOf(MockWhatsAppProvider.class);
        });
    }

    @Test
    void mockProviderIsActiveWhenPropertySetToMock() {
        runner.withPropertyValues("mnss.whatsapp.provider=mock")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(WhatsAppProviderPort.class);
                    assertThat(ctx.getBean(WhatsAppProviderPort.class))
                            .isInstanceOf(MockWhatsAppProvider.class);
                });
    }

    @Test
    void mockProviderIsNotActiveWhenPropertySetToAnotherValue() {
        runner.withPropertyValues("mnss.whatsapp.provider=real")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(MockWhatsAppProvider.class));
    }
}
