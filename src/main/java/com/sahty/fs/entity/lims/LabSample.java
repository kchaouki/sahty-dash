package com.sahty.fs.entity.lims;

import com.sahty.fs.entity.Admission;
import com.sahty.fs.entity.Patient;
import com.sahty.fs.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab_samples")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabSample {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(unique = true, nullable = false)
    private String sampleNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id")
    private Admission admission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specimen_id")
    private LabSpecimen specimen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id")
    private LabContainer container;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SampleStatus status = SampleStatus.REGISTERED;

    private LocalDateTime collectionDateTime;
    private LocalDateTime receptionDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id")
    private User collector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private String barcode;
    private String notes;
    private boolean isStat = false;

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabResult> results = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum SampleStatus { REGISTERED, COLLECTED, IN_TRANSIT, RECEIVED, IN_PROGRESS, RESULTED, VALIDATED, REJECTED }
}
