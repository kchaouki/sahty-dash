package com.sahty.fs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tenant_groups")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TenantGroup {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, unique = true)
    private String name;
    private String description;
    @OneToMany(mappedBy = "group")
    private Set<Tenant> tenants = new HashSet<>();
}
