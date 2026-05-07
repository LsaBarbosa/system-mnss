package br.com.novaalianca.mnss.onlineapp.domain.user;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class OnlineRoleEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    protected OnlineRoleEntity() {}

    public String getName() {
        return name;
    }
}
