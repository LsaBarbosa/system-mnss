package br.com.novaalianca.mnss.onlineapp.security.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtTokenProvider {

    @Value("${mnss.online.jwt-secret:change_me_secret}")
    private String tokenSecret;

    public String generateToken(String username) {
        long expiresAt = System.currentTimeMillis() + (8L * 60 * 60 * 1000);
        String header  = b64url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = b64url("{\"sub\":\"" + username + "\",\"exp\":" + expiresAt + "}");
        String sig     = hmacSha256(header + "." + payload);
        return header + "." + payload + "." + sig;
    }

    public String extractUsername(String token) {
        try {
            String[] parts = splitAndVerify(token);
            if (parts == null) return null;
            String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return extractJsonString(json, "sub");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = splitAndVerify(token);
            if (parts == null) return false;
            String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String expStr = extractJsonString(json, "exp");
            if (expStr == null) return false;
            return System.currentTimeMillis() < Long.parseLong(expStr);
        } catch (Exception e) {
            return false;
        }
    }

    private String[] splitAndVerify(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        String expected = hmacSha256(parts[0] + "." + parts[1]);
        return expected.equals(parts[2]) ? parts : null;
    }

    private String b64url(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign token", e);
        }
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int start = idx + search.length();
        boolean quoted = json.charAt(start) == '"';
        if (quoted) start++;
        int end = quoted
                ? json.indexOf('"', start)
                : json.indexOf('}', start);
        if (end < 0) return null;
        String value = json.substring(start, end).trim();
        return value.endsWith(",") ? value.substring(0, value.length() - 1) : value;
    }
}
