package com.sahty.fs.entity.lims;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_methods")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabMethod {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    private String code;
    private String description;
    private String principle;
    private boolean isActive = true;
}
