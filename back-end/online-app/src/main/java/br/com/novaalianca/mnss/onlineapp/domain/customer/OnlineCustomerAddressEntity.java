package br.com.novaalianca.mnss.onlineapp.domain.customer;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "customer_addresses")
public class OnlineCustomerAddressEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private OnlineCustomerEntity customer;

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

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    private String reference;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Column(name = "default_address", nullable = false)
    private boolean defaultAddress = false;

    protected OnlineCustomerAddressEntity() {}

    public OnlineCustomerAddressEntity(OnlineCustomerEntity customer, String street, String number, String neighborhood, String city, String state, String zipCode) {
        if (customer == null) throw new IllegalArgumentException("Customer is required");
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Street is required");
        this.customer = customer;
        this.street = street;
        this.number = number;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    public OnlineCustomerEntity getCustomer() { return customer; }
    public String getLabel() { return label; }
    public String getStreet() { return street; }
    public String getNumber() { return number; }
    public String getComplement() { return complement; }
    public String getNeighborhood() { return neighborhood; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public String getReference() { return reference; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public boolean isDefaultAddress() { return defaultAddress; }
}
