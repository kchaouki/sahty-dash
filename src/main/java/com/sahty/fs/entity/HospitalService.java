package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hospital_services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HospitalService {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    @Column(nullable = false)
    private String name;
    private String code;
    private String description;
    private String color;
    private boolean isActive = true;
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();
}
