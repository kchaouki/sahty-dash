package com.sahty.fs.repository;

import com.sahty.fs.entity.Admission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, String> {

    List<Admission> findByPatientIdOrderByAdmissionDateDesc(String patientId);

    Optional<Admission> findByNda(String nda);

    @Query("""
        SELECT a FROM Admission a
        JOIN FETCH a.patient p
        WHERE a.tenant.id = :tenantId
        AND a.status = 'EN_COURS'
        ORDER BY a.admissionDate DESC
        """)
    List<Admission> findActiveByTenant(@Param("tenantId") String tenantId);

    @Query("""
        SELECT a FROM Admission a
        JOIN FETCH a.patient p
        WHERE a.tenant.id = :tenantId
        AND (:status IS NULL OR a.status = :status)
        ORDER BY a.admissionDate DESC
        """)
    Page<Admission> findByTenantAndStatus(@Param("tenantId") String tenantId,
                                           @Param("status") Admission.AdmissionStatus status,
                                           Pageable pageable);

    List<Admission> findByServiceIdAndStatus(String serviceId, Admission.AdmissionStatus status);

    long countByTenantIdAndStatus(String tenantId, Admission.AdmissionStatus status);
}
