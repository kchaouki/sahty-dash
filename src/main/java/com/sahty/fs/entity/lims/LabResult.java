package com.sahty.fs.entity.lims;

import com.sahty.fs.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabResult {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id", nullable = false)
    private LabSample sample;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyte_id", nullable = false)
    private LabAnalyte analyte;
    private String value;
    private String unit;
    private String referenceRange;
    @Enumerated(EnumType.STRING)
    private ResultFlag flag;
    @Enumerated(EnumType.STRING)
    private ResultStatus status = ResultStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_id")
    private User validatedBy;
    private LocalDateTime validatedAt;
    private String notes;
    @CreationTimestamp
    private LocalDateTime createdAt;
    public enum ResultFlag { NORMAL, LOW, HIGH, CRITICAL_LOW, CRITICAL_HIGH }
    public enum ResultStatus { PENDING, RESULTED, VALIDATED, CORRECTED }
}
