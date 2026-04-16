package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "antecedents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Antecedent {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AntecedentType type;
    @Column(nullable = false)
    private String description;
    private String icdCode;
    private String icdDescription;
    private Integer yearOnset;
    private String notes;
    @CreationTimestamp
    private LocalDateTime createdAt;
    public enum AntecedentType { MEDICAL, SURGICAL, FAMILY, OBSTETRIC, PSYCHIATRIC, TOXIC, VACCINATION }
}
