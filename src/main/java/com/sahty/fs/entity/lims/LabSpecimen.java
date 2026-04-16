package com.sahty.fs.entity.lims;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_specimens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabSpecimen {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    private String code;
    private String description;
    private String collectionInstructions;
    private boolean isActive = true;
}
