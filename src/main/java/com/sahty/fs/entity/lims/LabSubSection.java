package com.sahty.fs.entity.lims;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_sub_sections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabSubSection {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    private String code;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private LabSection section;
    private boolean isActive = true;
}
