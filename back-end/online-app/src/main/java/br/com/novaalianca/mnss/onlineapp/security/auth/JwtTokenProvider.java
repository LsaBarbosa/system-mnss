package br.com.novaalianca.mnss.onlineapp.security.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class JwtTokenProvider {
    @Value("${auth.token.secret:change_me_secret}")
    private String tokenSecret;

    public String generateToken(String username) {
        long expirationTime = System.currentTimeMillis() + (8 * 60 * 60 * 1000);
        String payload = username + ":" + expirationTime;
        return Base64.getEncoder().encodeToString(payload.getBytes());
    }

    public String extractUsername(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            return decoded.split(":")[0];
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length != 2) return false;
            long expirationTime = Long.parseLong(parts[1]);
            return System.currentTimeMillis() < expirationTime;
        } catch (Exception e) {
            return false;
        }
    }
}
