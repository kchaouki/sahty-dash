package com.sahty.fs.entity.pharmacy;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;
    private String atcCode;
    private String dciId;
    private String dciName;
    private String form;
    private String dosage;
    private String dosageUnit;
    private String packagingUnit;
    private Integer unitsPerPack;
    private String careCategoryId;
    private String therapeuticClass;
    @Column(columnDefinition = "text")
    private String description;
    private boolean isActive = true;
    private String tenantId;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSupplier> suppliers = new ArrayList<>();
    @CreationTimestamp
    private LocalDateTime createdAt;
    public enum ProductType { DRUG, CONSUMABLE, DEVICE, REAGENT }
}
