package br.com.novaalianca.mnss.localapp.security.user;

import br.com.novaalianca.mnss.localapp.domain.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new LinkedHashSet<>();

    protected UserEntity() {
    }

    public UserEntity(
            String name,
            String email,
            String username,
            String passwordHash,
            boolean active,
            Set<RoleEntity> roles) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
        replaceRoles(roles);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public Set<RoleEntity> getRoles() {
        return Set.copyOf(roles);
    }

    public void replaceRoles(Set<RoleEntity> roles) {
        this.roles = new LinkedHashSet<>(roles);
        touch();
    }
}
