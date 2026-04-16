package com.sahty.fs.entity.pharmacy;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "product_suppliers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductSupplier {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    private BigDecimal purchasePrice;
    private String currency = "MAD";
    private String supplierRef;
    private LocalDate priceValidFrom;
    private LocalDate priceValidTo;
    private boolean isPreferred;
}
