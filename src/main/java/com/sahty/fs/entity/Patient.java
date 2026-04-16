package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "tenant_patient_id")
    private String tenantPatientId;

    @Column(name = "global_patient_id")
    private String globalPatientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String ipp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LifecycleStatus lifecycleStatus = LifecycleStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private IdentityStatus identityStatus = IdentityStatus.PROVISIONAL;

    private String phone;
    private String email;
    private String homePhone;
    private String address;
    private String city;
    private String zipCode;
    private String country;
    private String nationality;
    private String maritalStatus;
    private String profession;
    private String bloodGroup;

    private String fatherName;
    private String motherName;
    private String fatherPhone;
    private String motherPhone;

    private boolean isPayant;
    private String mainOrgId;
    private String mainOrgRelationship;
    private String mainOrgRegistrationNumber;
    private String complementaryOrgId;

    // Guardian
    private String guardianFirstName;
    private String guardianLastName;
    private String guardianPhone;
    private String guardianRelationship;
    private String guardianIdType;
    private String guardianIdNumber;
    private String guardianAddress;
    private String guardianHabilitation;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IdentityDocument> identityDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coverage> coverages = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @OrderBy("admissionDate DESC")
    private List<Admission> admissions = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Allergy> allergies = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Antecedent> antecedents = new ArrayList<>();

    @Column(name = "merged_into_id")
    private String mergedIntoId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public String getFullName() { return firstName + " " + lastName; }

    public enum Gender { M, F, OTHER }
    public enum LifecycleStatus { ACTIVE, MERGED, INACTIVE }
    public enum IdentityStatus { UNKNOWN, PROVISIONAL, VERIFIED }
}
