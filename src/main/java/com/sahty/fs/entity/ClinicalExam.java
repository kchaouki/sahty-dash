package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "clinical_exams")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClinicalExam {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private Admission admission;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examiner_id")
    private User examiner;
    private Double temperature;
    private Integer heartRate;
    private Integer systolicBP;
    private Integer diastolicBP;
    private Integer respiratoryRate;
    private Integer oxygenSaturation;
    private Double weight;
    private Double height;
    private Double bmi;
    private String painScore;
    private Integer glasgowEye;
    private Integer glasgowVerbal;
    private Integer glasgowMotor;
    @Column(columnDefinition = "text") private String generalState;
    @Column(columnDefinition = "text") private String cardiacExam;
    @Column(columnDefinition = "text") private String pulmonaryExam;
    @Column(columnDefinition = "text") private String abdominalExam;
    @Column(columnDefinition = "text") private String neurologicalExam;
    @Column(columnDefinition = "text") private String otherFindings;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
