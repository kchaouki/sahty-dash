package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bed {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    @Column(nullable = false)
    private String label;
    @Enumerated(EnumType.STRING)
    private BedStatus status = BedStatus.AVAILABLE;
    private String currentPatientId;
    private String currentAdmissionId;
    public enum BedStatus { AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED }
}
