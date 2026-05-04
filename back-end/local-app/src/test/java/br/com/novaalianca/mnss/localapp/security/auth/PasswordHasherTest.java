package br.com.novaalianca.mnss.localapp.security.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void hashesPasswordWithoutKeepingPlainText() {
        String hash = passwordHasher.hash("secret");

        assertThat(hash).isNotEqualTo("secret");
        assertThat(hash).startsWith("pbkdf2$");
        assertThat(passwordHasher.matches("secret", hash)).isTrue();
        assertThat(passwordHasher.matches("invalid", hash)).isFalse();
    }
}
