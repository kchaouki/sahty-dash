package com.sahty.fs.entity.pharmacy;

import com.sahty.fs.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockMovement {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private StockLocation location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType;

    private Integer quantity;
    private String lot;
    private BigDecimal unitCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String admissionId;
    private String patientId;
    private String reference;
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum MovementType {
        STOCK_IN, STOCK_OUT, DISPENSATION, RETURN, TRANSFER_IN, TRANSFER_OUT,
        ADJUSTMENT_PLUS, ADJUSTMENT_MINUS, QUARANTINE, DESTRUCTION
    }
}
