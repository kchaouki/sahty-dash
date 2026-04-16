package com.sahty.fs.entity.pharmacy;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockItem {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private StockLocation location;

    private String lot;
    private LocalDate expirationDate;
    private String serial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContainerType containerType;

    private Integer quantityUnits;
    private Integer unitsPerBox;
    private Integer remainingUnits;

    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.AVAILABLE;

    private String originSealedBoxId;
    private String originOpenBoxId;

    @Enumerated(EnumType.STRING)
    private ItemOrigin origin;

    private BigDecimal unitCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private String purchaseOrderRef;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum ContainerType { SEALED_BOX, OPEN_BOX, LOOSE_UNITS }
    public enum ItemStatus { AVAILABLE, RESERVED, QUARANTINE, DISPENSED, EXPIRED, DESTROYED }
    public enum ItemOrigin { PURCHASE, RETURN_PATIENT, RETURN_SERVICE, ADJUSTMENT, TRANSFER }
}
