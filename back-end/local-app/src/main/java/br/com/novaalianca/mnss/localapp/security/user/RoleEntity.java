package br.com.novaalianca.mnss.localapp.security.user;

import br.com.novaalianca.mnss.localapp.domain.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    protected RoleEntity() {
    }

    public RoleEntity(RoleName name, String description) {
        this.name = name.name();
        this.description = description;
    }

    public RoleName getName() {
        return RoleName.valueOf(name);
    }

    public String getDescription() {
        return description;
    }
}
