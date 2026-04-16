package com.sahty.fs.entity.pharmacy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suppliers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String name;
    private String code;
    private String address;
    private String city;
    private String country;
    private String phone;
    private String email;
    private String contactPerson;
    private String taxId;
    private boolean isActive = true;
    private String tenantId;
}
