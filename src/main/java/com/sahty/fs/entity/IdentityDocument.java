package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "identity_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IdentityDocument {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;
    @Column(nullable = false)
    private String documentNumber;
    private String issuingCountry;
    private boolean isPrimary;
    public enum DocumentType { CIN, PASSPORT, CARTE_DE_SEJOUR, BIRTH_CERTIFICATE, OTHER }
}
