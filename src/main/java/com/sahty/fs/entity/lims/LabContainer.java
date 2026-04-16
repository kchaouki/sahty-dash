package com.sahty.fs.entity.lims;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_containers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabContainer {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    private String code;
    private String color;
    private String additiveType;
    private String volume;
    private String description;
    private boolean isActive = true;
}
