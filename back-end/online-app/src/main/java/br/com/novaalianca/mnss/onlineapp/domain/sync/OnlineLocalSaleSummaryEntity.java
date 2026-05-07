package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "online_local_sale_summaries")
public class OnlineLocalSaleSummaryEntity extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String storeId;

    @Column(nullable = false)
    private UUID localOrderId;

    private Long orderNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 50)
    private String paymentStatus;

    private Instant finishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> rawPayload;

    protected OnlineLocalSaleSummaryEntity() {}

    public OnlineLocalSaleSummaryEntity(String storeId, UUID localOrderId, Long orderNumber,
                                        BigDecimal totalAmount, String paymentStatus,
                                        Instant finishedAt, Map<String, Object> rawPayload) {
        this.storeId = storeId;
        this.localOrderId = localOrderId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.finishedAt = finishedAt;
        this.rawPayload = rawPayload;
    }

    public String getStoreId() { return storeId; }
    public UUID getLocalOrderId() { return localOrderId; }
    public Long getOrderNumber() { return orderNumber; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public Instant getFinishedAt() { return finishedAt; }
}
