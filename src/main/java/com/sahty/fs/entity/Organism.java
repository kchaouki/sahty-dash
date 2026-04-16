package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organismes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Organism {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    private String code;
    private String type;
    private String country;
    private String contactPhone;
    private String contactEmail;
    @Column(name = "active")
    private boolean isActive = true;
}
