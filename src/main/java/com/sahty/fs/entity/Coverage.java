package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "coverages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coverage {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organism_id")
    private Organism organism;
    private String policyNumber;
    private String groupNumber;
    private String planName;
    @Enumerated(EnumType.STRING)
    private CoverageType coverageType;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @Enumerated(EnumType.STRING)
    private CoverageStatus status = CoverageStatus.ACTIVE;
    @Enumerated(EnumType.STRING)
    private RelationshipCode relationshipToSubscriber = RelationshipCode.SELF;
    public enum CoverageType { PRIMARY, SECONDARY, COMPLEMENTARY }
    public enum CoverageStatus { ACTIVE, EXPIRED, CANCELLED }
    public enum RelationshipCode { SELF, SPOUSE, CHILD, OTHER }
}
