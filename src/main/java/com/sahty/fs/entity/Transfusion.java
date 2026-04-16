package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfusions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transfusion {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private Admission admission;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id")
    private User nurse;
    private String bloodProduct;
    private String bloodGroup;
    private String rhFactor;
    private String bagNumber;
    private Integer volumeMl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Enumerated(EnumType.STRING)
    private TransfusionStatus status = TransfusionStatus.PLANNED;
    @Column(columnDefinition = "text") private String preTransfusionNotes;
    @Column(columnDefinition = "text") private String postTransfusionNotes;
    private boolean adverseReaction;
    private String adverseReactionDetails;
    @CreationTimestamp
    private LocalDateTime createdAt;
    public enum TransfusionStatus { PLANNED, IN_PROGRESS, COMPLETED, STOPPED, ADVERSE_REACTION }
}
