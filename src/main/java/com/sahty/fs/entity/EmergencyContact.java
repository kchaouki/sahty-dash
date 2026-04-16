package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emergency_contacts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyContact {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @Column(nullable = false)
    private String name;
    private String relationship;
    @Column(nullable = false)
    private String phone;
}
