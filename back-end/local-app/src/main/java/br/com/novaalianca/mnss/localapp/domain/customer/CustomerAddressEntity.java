package br.com.novaalianca.mnss.localapp.domain.customer;

import br.com.novaalianca.mnss.localapp.domain.shared.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "customer_addresses")
public class CustomerAddressEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(length = 80)
    private String label;

    @Column(nullable = false, length = 150)
    private String street;

    @Column(length = 30)
    private String number;

    @Column(length = 120)
    private String complement;

    @Column(length = 120)
    private String neighborhood;

    @Column(length = 120)
    private String city;

    @Column(length = 60)
    private String state;

    @Column(length = 20)
    private String zipCode;

    @Column(columnDefinition = "text")
    private String reference;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean defaultAddress;

    protected CustomerAddressEntity() {}

    public CustomerAddressEntity(CustomerEntity customer, String street) {
        this.customer = Objects.requireNonNull(customer, "customer must not be null");
        this.street = DomainValidation.requireText(street, "street");
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public String getLabel() {
        return label;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getReference() {
        return reference;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }
}
