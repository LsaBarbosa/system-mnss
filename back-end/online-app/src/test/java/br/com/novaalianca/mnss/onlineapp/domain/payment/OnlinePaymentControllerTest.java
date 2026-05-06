package br.com.novaalianca.mnss.onlineapp.domain.payment;

import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OnlinePaymentController.class, properties = "mnss.payment.webhook-secret=test-secret")
class OnlinePaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OnlinePaymentService paymentService;

    @Test
    void handleWebhook_WithValidSignature_ShouldReturnOk() throws Exception {
        String payload = "{\"transactionId\":\"tx-1\",\"status\":\"PAID\"}";
        String signature = HmacUtils.calculateHmac(payload, "test-secret");

        mockMvc.perform(post("/api/public/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        verify(paymentService).processWebhook("tx-1", "PAID", payload);
    }

    @Test
    void handleWebhook_WithSha256PrefixInSignature_ShouldReturnOk() throws Exception {
        String payload = "{\"transactionId\":\"tx-1\",\"status\":\"PAID\"}";
        String signature = HmacUtils.calculateHmac(payload, "test-secret");

        mockMvc.perform(post("/api/public/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Signature", "sha256=" + signature)
                        .content(payload))
                .andExpect(status().isOk());

        verify(paymentService).processWebhook("tx-1", "PAID", payload);
    }

    @Test
    void handleWebhook_WithoutSignature_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/public/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transactionId\":\"tx-1\",\"status\":\"PAID\"}"))
                .andExpect(status().isUnauthorized());

        verify(paymentService, never()).processWebhook(anyString(), anyString(), anyString());
    }

    @Test
    void handleWebhook_WithInvalidSignature_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/public/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Signature", "invalid-signature")
                        .content("{\"transactionId\":\"tx-1\",\"status\":\"PAID\"}"))
                .andExpect(status().isUnauthorized());

        verify(paymentService, never()).processWebhook(anyString(), anyString(), anyString());
    }

    @Test
    void handleWebhook_WithoutRequiredFields_ShouldReturnBadRequest() throws Exception {
        String payload = "{\"status\":\"PAID\"}";
        String signature = HmacUtils.calculateHmac(payload, "test-secret");

        mockMvc.perform(post("/api/public/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Signature", signature)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).processWebhook(anyString(), anyString(), anyString());
    }
}
