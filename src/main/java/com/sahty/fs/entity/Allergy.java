package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "allergies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Allergy {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @Column(nullable = false)
    private String allergen;
    @Enumerated(EnumType.STRING)
    private AllergenType allergenType;
    @Enumerated(EnumType.STRING)
    private Severity severity;
    private String reactions;
    private String notes;
    private boolean isActive = true;
    @CreationTimestamp
    private LocalDateTime createdAt;
    public enum AllergenType { DRUG, FOOD, ENVIRONMENTAL, LATEX, OTHER }
    public enum Severity { MILD, MODERATE, SEVERE, LIFE_THREATENING }
}
