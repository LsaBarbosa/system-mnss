package br.com.novaalianca.mnss.sharedinfra.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class HmacUtils {
    private static final String ALGORITHM = "HmacSHA256";

    private HmacUtils() {}

    public static String calculateHmac(String data, String secret) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC", e);
        }
    }

    public static boolean verifyHmac(String data, String hmac, String secret) {
        String calculatedHmac = calculateHmac(data, secret);
        return calculatedHmac.equals(hmac);
    }
}
