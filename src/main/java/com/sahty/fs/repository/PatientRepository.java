package com.sahty.fs.repository;

import com.sahty.fs.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {

    List<Patient> findByTenantIdAndLifecycleStatus(String tenantId, Patient.LifecycleStatus status);

    Optional<Patient> findByTenantIdAndIpp(String tenantId, String ipp);

    @Query("""
        SELECT p FROM Patient p
        WHERE p.tenant.id = :tenantId
        AND p.lifecycleStatus = 'ACTIVE'
        AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(p.ipp) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY p.lastName, p.firstName
        """)
    Page<Patient> searchByTenant(@Param("tenantId") String tenantId,
                                  @Param("q") String query,
                                  Pageable pageable);

    @Query("""
        SELECT p FROM Patient p
        WHERE p.tenant.id = :tenantId
        AND p.lifecycleStatus = 'ACTIVE'
        ORDER BY p.createdAt DESC
        """)
    Page<Patient> findActiveByTenant(@Param("tenantId") String tenantId, Pageable pageable);

    long countByTenantIdAndLifecycleStatus(String tenantId, Patient.LifecycleStatus status);
}
