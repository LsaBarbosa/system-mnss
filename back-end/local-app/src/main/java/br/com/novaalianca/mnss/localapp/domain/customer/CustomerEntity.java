package br.com.novaalianca.mnss.localapp.domain.customer;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "customers")
public class CustomerEntity extends BaseEntity {
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String document;

    private LocalDate birthDate;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CustomerAddressEntity> addresses = new LinkedHashSet<>();

    protected CustomerEntity() {}

    public CustomerEntity(String name) {
        this.name = DomainValidation.requireText(name, "name");
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getDocument() {
        return document;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Set<CustomerAddressEntity> getAddresses() {
        return Set.copyOf(addresses);
    }
}
