package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tenants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Display name / commercial name of the hospital */
    @Column(name = "designation")
    private String designation;

    /** Legal head office name */
    @Column(name = "siege_social")
    private String siegeSocial;

    /** Legal representative name */
    @Column(name = "representant_legal")
    private String representantLegal;

    /** Tenant type (e.g. CLINIC, HOSPITAL, LAB) */
    @Column(name = "type")
    private String type;

    /** Tenancy mode (e.g. SHARED, DEDICATED) */
    @Column(name = "tenancy_mode")
    private String tenancyMode;

    private String address;
    private String city;
    private String country;
    private String phone;
    private String email;

    @Column(name = "db_name")
    private String dbName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private TenantGroup group;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<User> users = new HashSet<>();

    /** Convenience method used for display */
    public String getName() {
        return designation != null ? designation : (siegeSocial != null ? siegeSocial : id);
    }
}
