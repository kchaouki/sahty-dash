package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Prescription {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private Admission admission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescriber_id")
    private User prescriber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionType type;

    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    private String molecule;
    private String commercialName;
    private String dciId;
    private String productId;
    private Double qty;
    private String unit;
    private String route;
    private String adminMode;
    private Double adminDuration;
    private String adminDurationUnit;

    @Enumerated(EnumType.STRING)
    private ScheduleMode scheduleMode;

    private String scheduleType;
    private Integer interval;
    private String intervalUnit;
    private LocalDateTime startDateTime;
    private Integer durationDays;

    private String actId;
    private String actName;
    private String laboratorySection;
    private String imagingType;

    @Column(columnDefinition = "text")
    private String notes;

    private String bloodProduct;
    private String bloodGroup;
    private Integer transfusionUnits;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime pausedAt;
    private LocalDateTime stoppedAt;

    public enum PrescriptionType { MEDICATION, BIOLOGY, IMAGERY, CARE, PROCEDURE, TRANSFUSION }
    public enum PrescriptionStatus { ACTIVE, PAUSED, STOPPED, ELAPSED }
    public enum ScheduleMode { DAILY, WEEKLY, MONTHLY, ONE_TIME, CUSTOM }
}
