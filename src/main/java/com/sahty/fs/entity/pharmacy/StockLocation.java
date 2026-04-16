package com.sahty.fs.entity.pharmacy;

import com.sahty.fs.entity.HospitalService;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockLocation {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private LocationType type = LocationType.PHYSICAL;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private LocationScope scope = LocationScope.PHARMACY;
    @Enumerated(EnumType.STRING)
    private LocationClass locationClass = LocationClass.COMMERCIAL;
    @Enumerated(EnumType.STRING)
    private ValuationPolicy valuationPolicy = ValuationPolicy.VALUABLE;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private HospitalService service;
    private boolean isActive = true;
    public enum LocationType { PHYSICAL, VIRTUAL }
    public enum LocationScope { PHARMACY, SERVICE }
    public enum LocationClass { COMMERCIAL, CHARITY }
    public enum ValuationPolicy { VALUABLE, NON_VALUABLE }
}
