package com.sahty.fs.repository.lims;

import com.sahty.fs.entity.lims.LabSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabSampleRepository extends JpaRepository<LabSample, String> {

    Optional<LabSample> findBySampleNumber(String sampleNumber);

    @Query("""
        SELECT s FROM LabSample s
        LEFT JOIN FETCH s.patient
        LEFT JOIN FETCH s.admission
        LEFT JOIN FETCH s.specimen
        LEFT JOIN FETCH s.container
        WHERE s.patient.id = :patientId
        ORDER BY s.createdAt DESC
        """)
    List<LabSample> findByPatientIdOrderByCreatedAtDesc(@Param("patientId") String patientId);

    @Query("""
        SELECT s FROM LabSample s
        LEFT JOIN FETCH s.patient
        LEFT JOIN FETCH s.admission
        LEFT JOIN FETCH s.specimen
        LEFT JOIN FETCH s.container
        WHERE s.admission.id = :admissionId
        ORDER BY s.createdAt DESC
        """)
    List<LabSample> findByAdmissionIdOrderByCreatedAtDesc(@Param("admissionId") String admissionId);

    @Query("""
        SELECT s FROM LabSample s
        LEFT JOIN FETCH s.patient
        LEFT JOIN FETCH s.admission
        LEFT JOIN FETCH s.specimen
        LEFT JOIN FETCH s.container
        WHERE s.tenantId = :tenantId
        AND s.status = :status
        ORDER BY s.createdAt DESC
        """)
    List<LabSample> findByTenantIdAndStatus(@Param("tenantId") String tenantId,
                                            @Param("status") LabSample.SampleStatus status);
}
