package br.com.novaalianca.mnss.onlineapp.domain.customer;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "customers")
public class OnlineCustomerEntity extends BaseEntity {
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String document;

    private LocalDate birthDate;

    @OneToMany(mappedBy = "customer")
    private Set<OnlineCustomerAddressEntity> addresses = new LinkedHashSet<>();

    protected OnlineCustomerEntity() {}

    public OnlineCustomerEntity(String name, String phone, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public void updateInfo(String name, String phone, String email) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (email != null) {
            this.email = email;
        }
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

    public Set<OnlineCustomerAddressEntity> getAddresses() {
        return Set.copyOf(addresses);
    }
}
