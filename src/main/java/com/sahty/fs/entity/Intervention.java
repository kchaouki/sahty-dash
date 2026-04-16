package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "interventions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Intervention {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private Admission admission;
    @Column(nullable = false)
    private String name;
    private String type;
    private String surgeonName;
    private String anesthesiologist;
    private String operatingRoom;
    private LocalDateTime plannedDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Enumerated(EnumType.STRING)
    private InterventionStatus status = InterventionStatus.PLANNED;
    @Column(columnDefinition = "text") private String preOpNotes;
    @Column(columnDefinition = "text") private String operativeReport;
    @Column(columnDefinition = "text") private String postOpNotes;
    private String complications;
    private String anesthesiaType;
    @CreationTimestamp
    private LocalDateTime createdAt;
    public enum InterventionStatus { PLANNED, IN_PROGRESS, COMPLETED, CANCELLED }
}
