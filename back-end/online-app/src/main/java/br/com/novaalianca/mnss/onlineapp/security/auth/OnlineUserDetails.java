package br.com.novaalianca.mnss.onlineapp.security.auth;

import br.com.novaalianca.mnss.onlineapp.domain.user.OnlineUserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class OnlineUserDetails implements UserDetails {

    private final UUID id;
    private final String name;
    private final String email;
    private final String username;
    private final String passwordHash;
    private final boolean active;
    private final Collection<GrantedAuthority> authorities;

    public OnlineUserDetails(OnlineUserEntity user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.active = user.isActive();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return passwordHash; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isEnabled() { return active; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
