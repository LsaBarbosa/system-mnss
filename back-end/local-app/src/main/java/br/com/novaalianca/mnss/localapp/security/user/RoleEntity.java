package br.com.novaalianca.mnss.localapp.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected RoleEntity() {
    }

    public RoleEntity(RoleName name, String description) {
        this.name = name.name();
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public RoleName getName() {
        return RoleName.valueOf(name);
    }

    public String getDescription() {
        return description;
    }
}
