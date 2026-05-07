package br.com.novaalianca.mnss.onlineapp.domain.user;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class OnlineUserEntity extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<OnlineRoleEntity> roles = new LinkedHashSet<>();

    protected OnlineUserEntity() {}

    public OnlineUserEntity(String name, String email, String username, String passwordHash) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public void addRole(OnlineRoleEntity role) {
        roles.add(role);
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isActive() { return active; }
    public Set<OnlineRoleEntity> getRoles() { return Set.copyOf(roles); }
}
