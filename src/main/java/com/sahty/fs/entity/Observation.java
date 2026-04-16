package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "observations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Observation {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private Admission admission;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObservationType type;
    @Column(columnDefinition = "text")
    private String content;
    @Column(columnDefinition = "text")
    private String richContent;
    private boolean isConfidential;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public enum ObservationType { MEDICAL, NURSING, SOCIAL, PSYCHOLOGICAL, PHYSIOTHERAPY, OTHER }
}
