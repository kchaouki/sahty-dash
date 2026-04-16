package com.sahty.fs.entity.lims;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab_sections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabSection {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    private String code;
    private String description;
    private boolean isActive = true;
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabSubSection> subSections = new ArrayList<>();
}
