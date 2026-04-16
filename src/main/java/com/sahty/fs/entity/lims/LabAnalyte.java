package com.sahty.fs.entity.lims;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_analytes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabAnalyte {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    private String code;
    private String unit;
    private String normalRangeMin;
    private String normalRangeMax;
    private String criticalRangeMin;
    private String criticalRangeMax;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private LabSection section;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_section_id")
    private LabSubSection subSection;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id")
    private LabMethod method;
    private boolean isActive = true;
}
