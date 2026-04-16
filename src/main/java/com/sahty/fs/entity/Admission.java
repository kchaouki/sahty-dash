package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String nda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private HospitalService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private String bedLabel;
    private String bedId;
    private String reason;
    private String doctorName;
    private String doctorId;

    @Enumerated(EnumType.STRING)
    private AdmissionType type;

    @Enumerated(EnumType.STRING)
    private ArrivalMode arrivalMode;

    private String provenance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdmissionStatus status = AdmissionStatus.EN_COURS;

    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;
    private String currency = "MAD";

    @OneToMany(mappedBy = "admission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> prescriptions = new ArrayList<>();

    @OneToMany(mappedBy = "admission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Observation> observations = new ArrayList<>();

    @OneToMany(mappedBy = "admission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClinicalExam> clinicalExams = new ArrayList<>();

    @OneToMany(mappedBy = "admission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transfusion> transfusions = new ArrayList<>();

    @OneToMany(mappedBy = "admission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Intervention> interventions = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AdmissionStatus { EN_COURS, SORTI, ANNULE }
    public enum AdmissionType { HOSPITALISATION, AMBULATOIRE, URGENCE, CHIRURGIE }
    public enum ArrivalMode { AMBULANCE, PERSONNEL, TRANSFERT, SMUR }
}
